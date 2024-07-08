package com.redis.om.spring.repository.query;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteQueryExecutor;
import com.redis.om.spring.repository.query.bloom.BloomQueryExecutor;
import com.redis.om.spring.repository.query.clause.QueryClause;
import com.redis.om.spring.repository.query.cuckoo.CuckooQueryExecutor;
import com.redis.om.spring.util.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.geo.Point;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.*;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.util.Pair;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Schema.FieldType;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.*;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;
import redis.clients.jedis.util.SafeEncoder;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RediSearchQuery implements RepositoryQuery {

  private static final Log logger = LogFactory.getLog(RediSearchQuery.class);

  private final QueryMethod queryMethod;
  private final RedisOMProperties redisOMProperties;
  private final boolean hasLanguageParameter;
  // aggregation fields
  private final List<Entry<String, String>> aggregationLoad = new ArrayList<>();
  private final List<Entry<String, String>> aggregationApply = new ArrayList<>();
  private final List<Group> aggregationGroups = new ArrayList<>();
  private final List<SortedField> aggregationSortedFields = new ArrayList<>();
  private final List<List<Pair<String, QueryClause>>> queryOrParts = new ArrayList<>();
  // for non @Param annotated dynamic names
  private final List<String> paramNames = new ArrayList<>();
  private final Class<?> domainType;
  private final RedisModulesOperations<String> modulesOperations;
  private final boolean isANDQuery;
  private final BloomQueryExecutor bloomQueryExecutor;
  private final CuckooQueryExecutor cuckooQueryExecutor;
  private final AutoCompleteQueryExecutor autoCompleteQueryExecutor;
  private final GsonBuilder gsonBuilder;
  private final RediSearchIndexer indexer;
  private RediSearchQueryType type;
  private String value;
  // query fields
  private String[] returnFields;
  private Integer offset;
  private Integer limit;
  private String sortBy;
  private Boolean sortAscending;
  private String[] aggregationFilter;
  private Integer aggregationSortByMax;
  private Long aggregationTimeout;
  private Boolean aggregationVerbatim;
  private Gson gson;
  private boolean isNullParamQuery;

  @SuppressWarnings("unchecked")
  public RediSearchQuery(//
      QueryMethod queryMethod, //
      RepositoryMetadata metadata, //
      RediSearchIndexer indexer, //
      QueryMethodEvaluationContextProvider evaluationContextProvider, //
      KeyValueOperations keyValueOperations, //
      RedisModulesOperations<?> rmo, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
      GsonBuilder gsonBuilder, //
      RedisOMProperties redisOMProperties //
  ) {
    logger.info(String.format("Creating %s query method", queryMethod.getName()));

    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.indexer = indexer;
    this.queryMethod = queryMethod;
    this.domainType = this.queryMethod.getEntityInformation().getJavaType();
    this.gsonBuilder = gsonBuilder;
    this.redisOMProperties = redisOMProperties;

    bloomQueryExecutor = new BloomQueryExecutor(this, modulesOperations);
    cuckooQueryExecutor = new CuckooQueryExecutor(this, modulesOperations);
    autoCompleteQueryExecutor = new AutoCompleteQueryExecutor(this, modulesOperations);

    Class<?> repoClass = metadata.getRepositoryInterface();
    @SuppressWarnings("rawtypes") Class[] params = queryMethod.getParameters().stream().map(Parameter::getType)
        .toArray(Class[]::new);
    hasLanguageParameter = Arrays.stream(params).anyMatch(c -> c.isAssignableFrom(SearchLanguage.class));
    isANDQuery = QueryClause.hasContainingAllClause(queryMethod.getName());

    String methodName = isANDQuery ?
        QueryClause.getPostProcessMethodName(queryMethod.getName()) :
        queryMethod.getName();

    try {
      java.lang.reflect.Method method = repoClass.getMethod(queryMethod.getName(), params);
      if (method.isAnnotationPresent(com.redis.om.spring.annotations.Query.class)) {
        com.redis.om.spring.annotations.Query queryAnnotation = method.getAnnotation(
            com.redis.om.spring.annotations.Query.class);
        this.type = RediSearchQueryType.QUERY;
        this.value = queryAnnotation.value();
        this.returnFields = queryAnnotation.returnFields();
        this.offset = queryAnnotation.offset();
        this.limit = queryAnnotation.limit();
        this.sortBy = queryAnnotation.sortBy();
        this.sortAscending = queryAnnotation.sortAscending();
      } else if (method.isAnnotationPresent(Aggregation.class)) {
        Aggregation aggregation = method.getAnnotation(Aggregation.class);
        this.type = RediSearchQueryType.AGGREGATION;
        this.value = aggregation.value();
        Arrays.stream(aggregation.load())
            .forEach(load -> aggregationLoad.add(new SimpleEntry<>(load.property(), load.alias())));
        Arrays.stream(aggregation.apply())
            .forEach(apply -> aggregationApply.add(new SimpleEntry<>(apply.alias(), apply.expression())));
        this.aggregationFilter = aggregation.filter();
        this.aggregationTimeout = aggregation.timeout() > Long.MIN_VALUE ? aggregation.timeout() : null;
        this.aggregationVerbatim = aggregation.verbatim() ? true : null;
        this.aggregationSortByMax = aggregation.sortByMax() > Integer.MIN_VALUE ? aggregation.sortByMax() : null;
        this.limit = aggregation.limit() > Integer.MIN_VALUE ? aggregation.limit() : null;
        this.offset = aggregation.offset() > Integer.MIN_VALUE ? aggregation.offset() : null;
        Arrays.stream(aggregation.groupBy()).forEach(groupBy -> {
          Group group = new Group(groupBy.properties());
          Arrays.stream(groupBy.reduce()).forEach(reducer -> {
            String alias = reducer.alias();
            String arg0 = reducer.args().length > 0 ? reducer.args()[0] : null;

            redis.clients.jedis.search.aggr.Reducer r = null;
            switch (reducer.func()) {
              case COUNT -> r = Reducers.count();
              case COUNT_DISTINCT -> r = Reducers.count_distinct(arg0);
              case COUNT_DISTINCTISH -> r = Reducers.count_distinctish(arg0);
              case SUM -> r = Reducers.sum(arg0);
              case MIN -> r = Reducers.min(arg0);
              case MAX -> r = Reducers.max(arg0);
              case AVG -> r = Reducers.avg(arg0);
              case STDDEV -> r = Reducers.stddev(arg0);
              case QUANTILE -> {
                double percentile = Double.parseDouble(reducer.args()[1]);
                r = Reducers.quantile(arg0, percentile);
              }
              case TOLIST -> r = Reducers.to_list(arg0);
              case FIRST_VALUE -> {
                if (reducer.args().length > 1) {
                  String arg1 = reducer.args()[1];
                  String arg2 = reducer.args().length > 2 ? reducer.args()[2] : null;
                  SortOrder order = arg2 != null && arg2.equalsIgnoreCase("ASC") ? SortOrder.ASC : SortOrder.DESC;
                  SortedField sortedField = new SortedField(arg1, order);
                  r = Reducers.first_value(arg0, sortedField);
                } else {
                  r = Reducers.first_value(arg0);
                }
              }
              case RANDOM_SAMPLE -> {
                int sampleSize = Integer.parseInt(reducer.args()[1]);
                r = Reducers.random_sample(arg0, sampleSize);
              }
            }
            if (r != null && alias != null && !alias.isBlank()) {
              r.as(alias);
            }
            group.reduce(r);
          });
          aggregationGroups.add(group);
        });
        Arrays.stream(aggregation.sortBy()).forEach(sb -> {
          SortedField sortedField = sb.direction().isAscending() ?
              SortedField.asc(sb.field()) :
              SortedField.desc(sb.field());
          aggregationSortedFields.add(sortedField);
        });

      } else if (queryMethod.getName().equalsIgnoreCase("search")) {
        this.type = RediSearchQueryType.QUERY;
        List<Pair<String, QueryClause>> orPartParts = new ArrayList<>();
        orPartParts.add(Pair.of("__ALL__", QueryClause.TEXT_ALL));
        queryOrParts.add(orPartParts);
        this.returnFields = new String[] {};
      } else if (queryMethod.getName().startsWith("getAll")) {
        this.type = RediSearchQueryType.TAGVALS;
        this.value = ObjectUtils.toLowercaseFirstCharacter(queryMethod.getName().substring(6));
      } else if (queryMethod.getName().startsWith(AutoCompleteQueryExecutor.AUTOCOMPLETE_PREFIX)) {
        this.type = RediSearchQueryType.AUTOCOMPLETE;
      } else {
        PartTree pt = new PartTree(methodName, metadata.getDomainType());

        List<String> nullParamNames = new ArrayList<>();
        List<String> notNullParamNames = new ArrayList<>();

        pt.getParts().forEach(part -> {
          if (part.getType() == Part.Type.IS_NULL) {
            nullParamNames.add(part.getProperty().getSegment());
          } else if (part.getType() == Part.Type.IS_NOT_NULL) {
            notNullParamNames.add(part.getProperty().getSegment());
          }
        });

        this.isNullParamQuery = !nullParamNames.isEmpty() || !notNullParamNames.isEmpty();

        this.type = queryMethod.getName().matches("(?:remove|delete).*") ?
            RediSearchQueryType.DELETE :
            RediSearchQueryType.QUERY;
        this.returnFields = new String[] {};
        processPartTree(pt, nullParamNames, notNullParamNames);
      }
    } catch (NoSuchMethodException | SecurityException e) {
      logger.debug(
          String.format("Could not resolved query method %s(%s): %s", queryMethod.getName(), Arrays.toString(params),
              e.getMessage()));
    }
  }

  private void processPartTree(PartTree pt, List<String> nullParamNames, List<String> notNullParamNames) {
    pt.stream().forEach(orPart -> {
      List<Pair<String, QueryClause>> orPartParts = new ArrayList<>();
      orPart.iterator().forEachRemaining(part -> {
        PropertyPath propertyPath = part.getProperty();
        List<PropertyPath> path = StreamSupport.stream(propertyPath.spliterator(), false).toList();

        String paramName = path.get(path.size() - 1).getSegment();
        if (nullParamNames.contains(paramName)) {
          orPartParts.add(Pair.of(paramName, QueryClause.IS_NULL));
        } else if (notNullParamNames.contains(paramName)) {
          orPartParts.add(Pair.of(paramName, QueryClause.IS_NOT_NULL));
        } else {
          orPartParts.addAll(extractQueryFields(domainType, part, path));
        }
      });
      queryOrParts.add(orPartParts);
    });

    // Order By
    Optional<Order> maybeOrder = pt.getSort().stream().findFirst();
    if (maybeOrder.isPresent()) {
      Order order = maybeOrder.get();
      sortBy = order.getProperty();
      sortAscending = order.isAscending();
    }
  }

  private List<Pair<String, QueryClause>> extractQueryFields(Class<?> type, Part part, List<PropertyPath> path) {
    return extractQueryFields(type, part, path, 0);
  }

  private List<Pair<String, QueryClause>> extractQueryFields(Class<?> type, Part part, List<PropertyPath> path,
      int level) {
    List<Pair<String, QueryClause>> qf = new ArrayList<>();
    String property = path.get(level).getSegment();
    String key = part.getProperty().toDotPath().replace(".", "_");

    Field field = ReflectionUtils.findField(type, property);
    if (field == null) {
      logger.info(String.format("Did not find a field named %s", key));
      return qf;
    }
    if (field.isAnnotationPresent(TextIndexed.class)) {
      TextIndexed indexAnnotation = field.getAnnotation(TextIndexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      qf.add(Pair.of(actualKey, QueryClause.get(FieldType.TEXT, part.getType())));
    } else if (field.isAnnotationPresent(Searchable.class)) {
      Searchable indexAnnotation = field.getAnnotation(Searchable.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      qf.add(Pair.of(actualKey, QueryClause.get(FieldType.TEXT, part.getType())));
    } else if (field.isAnnotationPresent(TagIndexed.class)) {
      TagIndexed indexAnnotation = field.getAnnotation(TagIndexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      qf.add(Pair.of(actualKey, QueryClause.get(FieldType.TAG, part.getType())));
    } else if (field.isAnnotationPresent(GeoIndexed.class)) {
      GeoIndexed indexAnnotation = field.getAnnotation(GeoIndexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      qf.add(Pair.of(actualKey, QueryClause.get(FieldType.GEO, part.getType())));
    } else if (field.isAnnotationPresent(NumericIndexed.class)) {
      NumericIndexed indexAnnotation = field.getAnnotation(NumericIndexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      qf.add(Pair.of(actualKey, QueryClause.get(FieldType.NUMERIC, part.getType())));
    } else if (field.isAnnotationPresent(Indexed.class)) {
      Indexed indexAnnotation = field.getAnnotation(Indexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());
      //
      // Any Character class, Enums or Boolean -> Tag Search Field
      //
      if (CharSequence.class.isAssignableFrom(
          fieldType) || (fieldType == Boolean.class) || (fieldType == UUID.class) || (fieldType == Ulid.class) || (fieldType.isEnum())) {
        qf.add(Pair.of(actualKey, QueryClause.get(FieldType.TAG, part.getType())));
      }
      //
      // Any Numeric class -> Numeric Search Field
      //
      else if (Number.class.isAssignableFrom(
          fieldType) || (fieldType == LocalDateTime.class) || (field.getType() == LocalDate.class) || (field.getType() == Date.class)) {
        qf.add(Pair.of(actualKey, QueryClause.get(FieldType.NUMERIC, part.getType())));
      }
      //
      // Set / List
      //
      else if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)) {
        Optional<Class<?>> maybeCollectionType = ObjectUtils.getCollectionElementClass(field);
        if (maybeCollectionType.isPresent()) {
          Class<?> collectionType = maybeCollectionType.get();
          if (Number.class.isAssignableFrom(collectionType)) {
            if (isANDQuery) {
              qf.add(Pair.of(actualKey, QueryClause.NUMERIC_CONTAINING_ALL));
            } else {
              qf.add(Pair.of(actualKey, QueryClause.get(FieldType.NUMERIC, part.getType())));
            }
          } else if (collectionType == Point.class) {
            if (isANDQuery) {
              qf.add(Pair.of(actualKey, QueryClause.GEO_CONTAINING_ALL));
            } else {
              qf.add(Pair.of(actualKey, QueryClause.get(FieldType.GEO, part.getType())));
            }
          } else if (CharSequence.class.isAssignableFrom(collectionType) || (collectionType == Boolean.class)) {
            if (isANDQuery) {
              qf.add(Pair.of(actualKey, QueryClause.TAG_CONTAINING_ALL));
            } else {
              qf.add(Pair.of(actualKey, QueryClause.get(FieldType.TAG, part.getType())));
            }
          } else {
            qf.addAll(extractQueryFields(collectionType, part, path, level + 1));
          }
        }
      }
      //
      // Point
      //
      else if (fieldType == Point.class) {
        qf.add(Pair.of(actualKey, QueryClause.get(FieldType.GEO, part.getType())));
      }
      //
      // Recursively explore the fields for @Indexed annotated fields
      //
      else {
        qf.addAll(extractQueryFields(fieldType, part, path, level + 1));
      }
    }

    return qf;
  }

  @Override
  public Object execute(Object[] parameters) {
    Optional<String> maybeBloomFilter = bloomQueryExecutor.getBloomFilter();
    Optional<String> maybeCuckooFilter = cuckooQueryExecutor.getCuckooFilter();

    if (maybeBloomFilter.isPresent()) {
      return bloomQueryExecutor.executeBloomQuery(parameters, maybeBloomFilter.get());
    } else if (maybeCuckooFilter.isPresent()) {
      return cuckooQueryExecutor.executeCuckooQuery(parameters, maybeCuckooFilter.get());
    } else if (type == RediSearchQueryType.QUERY) {
      return !isNullParamQuery ? executeQuery(parameters) : executeNullQuery(parameters);
    } else if (type == RediSearchQueryType.AGGREGATION) {
      return executeAggregation(parameters);
    } else if (type == RediSearchQueryType.DELETE) {
      return executeDeleteQuery(parameters);
    } else if (type == RediSearchQueryType.TAGVALS) {
      return executeFtTagVals();
    } else if (type == RediSearchQueryType.AUTOCOMPLETE) {
      Optional<String> maybeAutoCompleteDictionaryKey = autoCompleteQueryExecutor.getAutoCompleteDictionaryKey();
      return maybeAutoCompleteDictionaryKey.map(s -> autoCompleteQueryExecutor.executeAutoCompleteQuery(parameters, s))
          .orElse(null);
    } else {
      return null;
    }
  }

  @Override
  public QueryMethod getQueryMethod() {
    return queryMethod;
  }

  private Object executeQuery(Object[] parameters) {
    ParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);
    ResultProcessor processor = queryMethod.getResultProcessor().withDynamicProjection(accessor);

    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    boolean excludeNullParams = !isNullParamQuery;
    String preparedQuery = prepareQuery(parameters, excludeNullParams);
    Query query = new Query(preparedQuery);

    ReturnedType returnedType = processor.getReturnedType();

    boolean isProjecting = returnedType.isProjecting() && returnedType.getReturnedType() != SearchResult.class;
    boolean isOpenProjecting = Arrays.stream(returnedType.getReturnedType().getMethods())
        .anyMatch(m -> m.isAnnotationPresent(Value.class));
    boolean canPerformQueryOptimization = isProjecting && !isOpenProjecting;

    if (canPerformQueryOptimization) {
      query.returnFields(returnedType.getInputProperties().stream()
          .map(inputProperty -> new FieldName("$." + inputProperty, inputProperty)).toArray(FieldName[]::new));
    } else {
      query.returnFields(returnFields);
    }

    Optional<Pageable> maybePageable = Optional.empty();

    boolean needsLimit = true;
    if (queryMethod.isPageQuery()) {
      maybePageable = Arrays.stream(parameters).filter(Pageable.class::isInstance).map(Pageable.class::cast)
          .findFirst();

      if (maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        if (!pageable.isUnpaged()) {
          query.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());
          needsLimit = false;

          for (Order order : pageable.getSort()) {
            var alias = QueryUtils.escape(indexer.getAlias(domainType, order.getProperty()));
            query.setSortBy(alias, order.isAscending());
          }
        }
      }
    }

    if (needsLimit) {
      if ((limit != null && limit != Integer.MIN_VALUE) || (offset != null && offset != Integer.MIN_VALUE)) {
        query.limit(offset != null ? offset : 0,
            limit != null ? limit : redisOMProperties.getRepository().getQuery().getLimit());
      } else {
        query.limit(0, redisOMProperties.getRepository().getQuery().getLimit());
      }
    }

    if ((sortBy != null && !sortBy.isBlank())) {
      var alias = indexer.getAlias(domainType, sortBy);
      query.setSortBy(alias, sortAscending);
    }

    if (hasLanguageParameter) {
      Optional<SearchLanguage> maybeSearchLanguage = Arrays.stream(parameters).filter(SearchLanguage.class::isInstance)
          .map(SearchLanguage.class::cast).findFirst();
      maybeSearchLanguage.ifPresent(searchLanguage -> query.setLanguage(searchLanguage.getValue()));
    }

    // Intercept TAG collection queries with empty parameters and use an
    // aggregation
    if (queryMethod.isCollectionQuery() && !queryMethod.getParameters().isEmpty()) {
      List<Collection<?>> emptyCollectionParams = Arrays.stream(parameters) //
          .filter(Collection.class::isInstance) //
          .map(p -> (Collection<?>) p) //
          .filter(Collection::isEmpty) //
          .collect(Collectors.toList());
      if (!emptyCollectionParams.isEmpty()) {
        return Collections.emptyList();
      }
    }

    SearchResult searchResult = ops.search(query);

    // what to return
    Object result = null;
    if (queryMethod.getReturnedObjectType() == SearchResult.class) {
      result = searchResult;
    } else if (queryMethod.isPageQuery()) {
      List<Object> content = searchResult.getDocuments().stream().map(this::parseDocumentResult).toList();

      if (maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        result = new PageImpl<>(content, pageable, searchResult.getTotalResults());
      }

    } else if (!queryMethod.isCollectionQuery()) {
      // handle the case where we have a single entity result and we the query results are empty
      if (!searchResult.getDocuments().isEmpty()) {
        redis.clients.jedis.search.Document doc = searchResult.getDocuments().get(0);
        result = parseDocumentResult(doc);
      }
    } else if ((queryMethod.isCollectionQuery()) || this.type == RediSearchQueryType.DELETE) {
      result = searchResult.getDocuments().stream().map(this::parseDocumentResult).toList();
    }

    return processor.processResult(result);
  }

  private Object parseDocumentResult(redis.clients.jedis.search.Document doc) {
    if (doc == null || doc.get("$") == null) {
      return null;
    }

    Gson gsonInstance = getGson();

    return gsonInstance.fromJson(SafeEncoder.encode((byte[]) doc.get("$")), domainType);
  }

  private Object executeDeleteQuery(Object[] parameters) {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    String baseQuery = prepareQuery(parameters, true);
    AggregationBuilder aggregation = new AggregationBuilder(baseQuery);

    // Load fields with IS_NULL or IS_NOT_NULL query clauses
    String[] fields = Stream.concat(Stream.of("@__key"), queryOrParts.stream().flatMap(List::stream)
        .filter(pair -> pair.getSecond() == QueryClause.IS_NULL || pair.getSecond() == QueryClause.IS_NOT_NULL)
        .map(pair -> String.format("@%s", pair.getFirst()))).toArray(String[]::new);
    aggregation.load(fields);

    // Apply exists or !exists filter for null parameters
    for (List<Pair<String, QueryClause>> orPartParts : queryOrParts) {
      for (Pair<String, QueryClause> pair : orPartParts) {
        if (pair.getSecond() == QueryClause.IS_NULL) {
          aggregation.filter("!exists(@" + pair.getFirst() + ")");
        } else if (pair.getSecond() == QueryClause.IS_NOT_NULL) {
          aggregation.filter("exists(@" + pair.getFirst() + ")");
        }
      }
    }

    aggregation.sortBy(aggregationSortedFields.toArray(new SortedField[] {}));
    aggregation.limit(0, redisOMProperties.getRepository().getQuery().getLimit());

    // Execute the aggregation query
    AggregationResult aggregationResult = ops.aggregate(aggregation);

    // extract the keys from the aggregation result
    List<String> keys = aggregationResult.getResults().stream().map(d -> d.get("__key").toString()).toList();

    // determine if we need to return the deleted entities or just obtain the keys
    Class<?> returnType = queryMethod.getReturnedObjectType();
    if (Number.class.isAssignableFrom(returnType) || returnType.equals(int.class) || returnType.equals(
        long.class) || returnType.equals(short.class)) {
      // return the number of deleted entities, so we only need the ids
      if (keys.isEmpty()) {
        return 0;
      } else {
        return modulesOperations.template().delete(keys);
      }
    } else {
      if (keys.isEmpty()) {
        return Collections.emptyList();
      } else {
        // return the deleted entities
        var entities = modulesOperations.opsForJSON().mget(this.domainType, keys.toArray(new String[0]));
        modulesOperations.template().delete(keys);
        return entities;
      }
    }
  }

  private Object executeAggregation(Object[] parameters) {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);

    // build the aggregation
    AggregationBuilder aggregation = new AggregationBuilder(value);

    // timeout
    if (aggregationTimeout != null) {
      aggregation.timeout(aggregationTimeout);
    }

    // verbatim
    if (aggregationVerbatim != null) {
      aggregation.verbatim();
    }

    // load
    for (Map.Entry<String, String> apply : aggregationLoad) {
      if (apply.getValue().isBlank()) {
        aggregation.load(apply.getKey());
      } else {
        aggregation.load(apply.getKey(), "AS", apply.getValue());
      }
    }

    // group by
    aggregationGroups.forEach(aggregation::groupBy);

    // filter
    if (aggregationFilter != null) {
      for (String filter : aggregationFilter) {
        aggregation.filter(filter);
      }
    }

    // sort by
    Optional<Pageable> maybePageable = Optional.empty();

    boolean needsLimit = true;
    if (queryMethod.isPageQuery()) {
      maybePageable = Arrays.stream(parameters).filter(Pageable.class::isInstance).map(Pageable.class::cast)
          .findFirst();

      if (maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        if (!pageable.isUnpaged()) {
          aggregation.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());
          needsLimit = false;

          // sort by
          pageable.getSort();
          for (Order order : pageable.getSort()) {
            var alias = indexer.getAlias(domainType, order.getProperty());
            if (order.isAscending()) {
              aggregation.sortByAsc(alias);
            } else {
              aggregation.sortByDesc(alias);
            }
          }
        }
      }
    }

    if ((sortBy != null && !sortBy.isBlank())) {
      var alias = indexer.getAlias(domainType, sortBy);
      aggregation.sortByAsc(alias);
    } else if (!aggregationSortedFields.isEmpty()) {
      if (aggregationSortByMax != null) {
        aggregation.sortBy(aggregationSortByMax, aggregationSortedFields.toArray(new SortedField[] {}));
      } else {
        aggregation.sortBy(aggregationSortedFields.toArray(new SortedField[] {}));
      }
    }

    // apply
    for (Map.Entry<String, String> apply : aggregationApply) {
      aggregation.apply(apply.getValue(), apply.getKey());
    }

    // limit
    if (needsLimit) {
      if ((limit != null) || (offset != null)) {
        aggregation.limit(offset != null ? offset : 0, limit != null ? limit : 0);
      } else {
        aggregation.limit(0, redisOMProperties.getRepository().getQuery().getLimit());
      }
    }

    // execute the aggregation
    AggregationResult aggregationResult = ops.aggregate(aggregation);

    // what to return
    Object result = null;
    if (queryMethod.getReturnedObjectType() == AggregationResult.class) {
      result = aggregationResult;
    } else if (queryMethod.getReturnedObjectType() == Map.class) {
      List<?> content = List.of();
      if (queryMethod.getReturnedObjectType() == Map.class) {
        content = aggregationResult.getResults().stream().map(m -> m.entrySet().stream() //
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue() != null ? e.getValue().toString() : "")) //
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)) //
        ).collect(Collectors.toList());
      }
      if (queryMethod.isPageQuery() && maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        result = new PageImpl<>(content, pageable, aggregationResult.getTotalResults());
      }
    }

    return result;
  }

  private Object executeFtTagVals() {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);

    return ops.tagVals(this.value);
  }

  private String prepareQuery(final Object[] parameters, boolean excludeNullParams) {
    logger.debug(String.format("parameters: %s", Arrays.toString(parameters)));
    List<Object> params = new ArrayList<>(Arrays.asList(parameters));
    StringBuilder preparedQuery = new StringBuilder();
    boolean multipleOrParts = queryOrParts.size() > 1;
    logger.debug(String.format("queryOrParts: %s", queryOrParts.size()));
    if (!queryOrParts.isEmpty()) {
      preparedQuery.append(queryOrParts.stream().map(qop -> {
        String orPart = multipleOrParts ? "(" : "";
        orPart = orPart + qop.stream().map(fieldClauses -> {
          if (excludeNullParams && (fieldClauses.getSecond() == QueryClause.IS_NULL || fieldClauses.getSecond() == QueryClause.IS_NOT_NULL)) {
            return "";
          }
          String fieldName = QueryUtils.escape(fieldClauses.getFirst());
          QueryClause queryClause = fieldClauses.getSecond();
          int paramsCnt = queryClause.getClauseTemplate().getNumberOfArguments();

          Object[] ps = params.subList(0, paramsCnt).toArray();
          params.subList(0, paramsCnt).clear();

          return queryClause.prepareQuery(fieldName, ps);
        }).collect(Collectors.joining(" "));
        orPart = orPart + (multipleOrParts ? ")" : "");

        return orPart;
      }).collect(Collectors.joining(" | ")));
    } else {
      @SuppressWarnings("unchecked") Iterator<Parameter> iterator = (Iterator<Parameter>) queryMethod.getParameters()
          .iterator();
      int index = 0;

      if (value != null && !value.isBlank()) {
        preparedQuery.append(value);
      }

      while (iterator.hasNext()) {
        Parameter p = iterator.next();
        Optional<String> maybeKey = p.getName();
        String key;
        if (maybeKey.isPresent()) {
          key = maybeKey.get();
        } else {
          key = paramNames.size() > index ? paramNames.get(index) : "";
        }

        if (!key.isBlank()) {
          String v;

          if (parameters[index] instanceof Collection<?> c) {
            v = c.stream().map(Object::toString).collect(Collectors.joining(" | "));
          } else {
            v = parameters[index].toString();
          }

          String regex = "\\$" + key + "\\b";
          preparedQuery = new StringBuilder(preparedQuery.toString().replaceAll(regex, v));
        }
        index++;
      }
    }

    if (preparedQuery.toString().isBlank()) {
      preparedQuery.append("*");
    }

    logger.debug(String.format("query: %s", preparedQuery));

    return preparedQuery.toString();
  }

  private Gson getGson() {
    if (gson == null) {
      gson = gsonBuilder.create();
    }
    return gson;
  }

  private Object executeNullQuery(Object[] parameters) {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    String baseQuery = prepareQuery(parameters, true);

    AggregationBuilder aggregation = new AggregationBuilder(baseQuery);

    // Load fields with IS_NULL or IS_NOT_NULL query clauses
    String[] fields = Stream.concat(Stream.of("@__key"), queryOrParts.stream().flatMap(List::stream)
        .filter(pair -> pair.getSecond() == QueryClause.IS_NULL || pair.getSecond() == QueryClause.IS_NOT_NULL)
        .map(pair -> String.format("@%s", pair.getFirst()))).toArray(String[]::new);

    aggregation.load(fields);

    // Apply exists or !exists filter for null parameters
    for (List<Pair<String, QueryClause>> orPartParts : queryOrParts) {
      for (Pair<String, QueryClause> pair : orPartParts) {
        if (pair.getSecond() == QueryClause.IS_NULL) {
          aggregation.filter("!exists(@" + pair.getFirst() + ")");
        } else if (pair.getSecond() == QueryClause.IS_NOT_NULL) {
          aggregation.filter("exists(@" + pair.getFirst() + ")");
        }
      }
    }

    // sort by
    Optional<Pageable> maybePageable;

    boolean needsLimit = true;
    if (queryMethod.isPageQuery()) {
      maybePageable = Arrays.stream(parameters).filter(Pageable.class::isInstance).map(Pageable.class::cast)
          .findFirst();

      if (maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        if (!pageable.isUnpaged()) {
          aggregation.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());
          needsLimit = false;

          // sort by
          pageable.getSort();
          for (Order order : pageable.getSort()) {
            var alias = indexer.getAlias(domainType, order.getProperty());
            if (order.isAscending()) {
              aggregation.sortByAsc(alias);
            } else {
              aggregation.sortByDesc(alias);
            }
          }
        }
      }
    }

    if ((sortBy != null && !sortBy.isBlank())) {
      var alias = indexer.getAlias(domainType, sortBy);
      aggregation.sortByAsc(alias);
    } else if (!aggregationSortedFields.isEmpty()) {
      if (aggregationSortByMax != null) {
        aggregation.sortBy(aggregationSortByMax, aggregationSortedFields.toArray(new SortedField[] {}));
      } else {
        aggregation.sortBy(aggregationSortedFields.toArray(new SortedField[] {}));
      }
    }

    // limit
    if (needsLimit) {
      if ((limit != null) || (offset != null)) {
        aggregation.limit(offset != null ? offset : 0, limit != null ? limit : 0);
      } else {
        aggregation.limit(0, redisOMProperties.getRepository().getQuery().getLimit());
      }
    }

    // Execute the aggregation query
    AggregationResult aggregationResult = ops.aggregate(aggregation);

    // extract the keys from the aggregation result
    String[] keys = aggregationResult.getResults().stream() //
        .map(d -> d.get("__key").toString()).toArray(String[]::new);

    var entities = modulesOperations.opsForJSON().mget(domainType, keys);

    if (!queryMethod.isCollectionQuery()) {
      return entities.isEmpty() ? null : entities.get(0);
    } else {
      return entities;
    }
  }
}
