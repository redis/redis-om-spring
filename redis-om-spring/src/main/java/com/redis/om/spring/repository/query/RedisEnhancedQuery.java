package com.redis.om.spring.repository.query;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.geo.Point;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.util.Pair;
import org.springframework.util.ClassUtils;

import com.redis.om.spring.annotations.Aggregation;
import com.redis.om.spring.annotations.GeoIndexed;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteQueryExecutor;
import com.redis.om.spring.repository.query.bloom.BloomQueryExecutor;
import com.redis.om.spring.repository.query.clause.QueryClause;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.AggregationResult;
import io.redisearch.Query;
import io.redisearch.Schema.FieldType;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.AggregationBuilder;

public class RedisEnhancedQuery implements RepositoryQuery {

  private static final Log logger = LogFactory.getLog(RedisEnhancedQuery.class);

  private final QueryMethod queryMethod;
  private final String searchIndex;

  private RediSearchQueryType type;
  private String value;

  @SuppressWarnings("unused")
  private RepositoryMetadata metadata;

  // query fields
  private String[] returnFields;

  // aggregation fields
  private String[] load;
  private Map<String, String> apply;

  //
  private List<List<Pair<String, QueryClause>>> queryOrParts = new ArrayList<>();

  // for non @Param annotated dynamic names
  private List<String> paramNames = new ArrayList<>();
  private Class<?> domainType;

  private RedisModulesOperations<String> modulesOperations;
  private MappingRedisOMConverter mappingConverter;
  @SuppressWarnings("unused")
  private RedisOperations<?, ?> redisOperations;

  private BloomQueryExecutor bloomQueryExecutor;
  private AutoCompleteQueryExecutor autoCompleteQueryExecutor;

  private boolean isANDQuery = false;

  @SuppressWarnings("unchecked")
  public RedisEnhancedQuery(QueryMethod queryMethod, //
      RepositoryMetadata metadata, //
      QueryMethodEvaluationContextProvider evaluationContextProvider, //
      KeyValueOperations keyValueOperations, //
      RedisOperations<?, ?> redisOperations, //
      RedisModulesOperations<?> rmo, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {
    logger.info(String.format("Creating query %s", queryMethod.getName()));

    this.redisOperations = redisOperations;
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.queryMethod = queryMethod;
    this.searchIndex = this.queryMethod.getEntityInformation().getJavaType().getName() + "Idx";
    this.metadata = metadata;
    this.domainType = this.queryMethod.getEntityInformation().getJavaType();

    this.mappingConverter = new MappingRedisOMConverter(null, new ReferenceResolverImpl(redisOperations));

    bloomQueryExecutor = new BloomQueryExecutor(this, modulesOperations);
    autoCompleteQueryExecutor = new AutoCompleteQueryExecutor(this, modulesOperations);

    Class<?> repoClass = metadata.getRepositoryInterface();
    @SuppressWarnings("rawtypes")
    Class[] params = queryMethod.getParameters().stream().map(p -> p.getType()).toArray(Class[]::new);
    try {
      java.lang.reflect.Method method = repoClass.getDeclaredMethod(queryMethod.getName(), params);
      if (method.isAnnotationPresent(com.redis.om.spring.annotations.Query.class)) {
        com.redis.om.spring.annotations.Query queryAnnotation = method
            .getAnnotation(com.redis.om.spring.annotations.Query.class);
        this.type = RediSearchQueryType.QUERY;
        this.value = queryAnnotation.value();
        this.returnFields = queryAnnotation.returnFields();
      } else if (method.isAnnotationPresent(com.redis.om.spring.annotations.Aggregation.class)) {
        Aggregation aggregation = method.getAnnotation(Aggregation.class);
        this.type = RediSearchQueryType.AGGREGATION;
        this.value = aggregation.value();
        this.load = aggregation.load();
        this.apply = splitApplyArguments(aggregation.apply());
      } else if (queryMethod.getName().equalsIgnoreCase("search")) {
        this.type = RediSearchQueryType.QUERY;
        List<Pair<String, QueryClause>> orPartParts = new ArrayList<>();
        orPartParts.add(Pair.of("__ALL__", QueryClause.FullText_ALL));
        queryOrParts.add(orPartParts);
        this.returnFields = new String[] {};
      } else if (queryMethod.getName().startsWith("getAll")) {
        this.type = RediSearchQueryType.TAGVALS;
        this.value = ObjectUtils.lcfirst(queryMethod.getName().substring(6, queryMethod.getName().length()));
      } else if (queryMethod.getName().startsWith(AutoCompleteQueryExecutor.AUTOCOMPLETE_PREFIX)) {
        this.type = RediSearchQueryType.AUTOCOMPLETE;
      } else {
        isANDQuery = QueryClause.hasContainingAllClause(queryMethod.getName());

        String methodName = isANDQuery ? QueryClause.getPostProcessMethodName(queryMethod.getName())
            : queryMethod.getName();

        PartTree pt = new PartTree(methodName, metadata.getDomainType());
        processPartTree(pt);

        this.type = RediSearchQueryType.QUERY;
        this.returnFields = new String[] {};
      }
    } catch (NoSuchMethodException | SecurityException e) {
      logger.debug(String.format("Could not resolved query method %s(%s): %s", queryMethod.getName(),
          Arrays.toString(params), e.getMessage()));
    }
  }

  private void processPartTree(PartTree pt) {
    pt.stream().forEach(orPart -> {
      List<Pair<String, QueryClause>> orPartParts = new ArrayList<>();
      orPart.iterator().forEachRemaining(part -> {
        PropertyPath propertyPath = part.getProperty();

        List<PropertyPath> path = StreamSupport.stream(propertyPath.spliterator(), false).collect(Collectors.toList());
        orPartParts.addAll(extractQueryFields(domainType, part, path));
      });
      queryOrParts.add(orPartParts);
    });
  }

  private List<Pair<String, QueryClause>> extractQueryFields(Class<?> type, Part part, List<PropertyPath> path) {
    return extractQueryFields(type, part, path, 0);
  }

  private List<Pair<String, QueryClause>> extractQueryFields(Class<?> type, Part part, List<PropertyPath> path,
      int level) {
    List<Pair<String, QueryClause>> qf = new ArrayList<>();
    String property = path.get(level).getSegment();
    String key = part.getProperty().toDotPath().replace(".", "_");

    Field field;
    try {
      field = type.getDeclaredField(property);

      if (field.isAnnotationPresent(TextIndexed.class)) {
        TextIndexed indexAnnotation = field.getAnnotation(TextIndexed.class);
        String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
        qf.add(Pair.of(actualKey, QueryClause.get(FieldType.FullText, part.getType())));
      } else if (field.isAnnotationPresent(Searchable.class)) {
        Searchable indexAnnotation = field.getAnnotation(Searchable.class);
        String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
        qf.add(Pair.of(actualKey, QueryClause.get(FieldType.FullText, part.getType())));
      } else if (field.isAnnotationPresent(TagIndexed.class)) {
        TagIndexed indexAnnotation = field.getAnnotation(TagIndexed.class);
        String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
        qf.add(Pair.of(actualKey, QueryClause.get(FieldType.Tag, part.getType())));
      } else if (field.isAnnotationPresent(GeoIndexed.class)) {
        GeoIndexed indexAnnotation = field.getAnnotation(GeoIndexed.class);
        String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
        qf.add(Pair.of(actualKey, QueryClause.get(FieldType.Geo, part.getType())));
      } else if (field.isAnnotationPresent(NumericIndexed.class)) {
        NumericIndexed indexAnnotation = field.getAnnotation(NumericIndexed.class);
        String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
        qf.add(Pair.of(actualKey, QueryClause.get(FieldType.Numeric, part.getType())));
      } else if (field.isAnnotationPresent(Indexed.class)) {
        Indexed indexAnnotation = field.getAnnotation(Indexed.class);
        String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
        Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());
        //
        // Any Character class or Boolean -> Tag Search Field
        //
        if (CharSequence.class.isAssignableFrom(fieldType) || (fieldType == Boolean.class)) {
          qf.add(Pair.of(actualKey, QueryClause.get(FieldType.Tag, part.getType())));
        }
        //
        // Any Numeric class -> Numeric Search Field
        //
        else if (Number.class.isAssignableFrom(fieldType) || (fieldType == LocalDateTime.class)
            || (field.getType() == LocalDate.class) || (field.getType() == Date.class)) {
          qf.add(Pair.of(actualKey, QueryClause.get(FieldType.Numeric, part.getType())));
        }
        //
        // Set / List
        //
        else if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)) {
          if (isANDQuery) {
            qf.add(Pair.of(actualKey, QueryClause.Tag_CONTAINING_ALL));
          } else {
            qf.add(Pair.of(actualKey, QueryClause.get(FieldType.Tag, part.getType())));
          }
        }
        //
        // Point
        //
        else if (fieldType == Point.class) {
          qf.add(Pair.of(actualKey, QueryClause.get(FieldType.Geo, part.getType())));
        }
        //
        // Recursively explore the fields for @Indexed annotated fields
        //
        else {
          qf.addAll(extractQueryFields(fieldType, part, path, level + 1));
        }
      }
    } catch (NoSuchFieldException e) {
      logger.info(String.format("Did not find a field named %s", key));
    }

    return qf;
  }

  @Override
  public Object execute(Object[] parameters) {
    Optional<String> maybeBloomFilter = bloomQueryExecutor.getBloomFilter();

    if (maybeBloomFilter.isPresent()) {
      return bloomQueryExecutor.executeBloomQuery(parameters, maybeBloomFilter.get());
    } else if (type == RediSearchQueryType.QUERY) {
      return executeQuery(parameters);
    } else if (type == RediSearchQueryType.AGGREGATION) {
      return executeAggregation(parameters);
    } else if (type == RediSearchQueryType.TAGVALS) {
      return executeFtTagVals();
    } else if (type == RediSearchQueryType.AUTOCOMPLETE) {
      Optional<String> maybeAutoCompleteDictionaryKey = autoCompleteQueryExecutor.getAutoCompleteDictionaryKey();
      return autoCompleteQueryExecutor.executeAutoCompleteQuery(parameters, maybeAutoCompleteDictionaryKey.get());
    } else {
      return null;
    }
  }

  @Override
  public QueryMethod getQueryMethod() {
    return queryMethod;
  }

  private Object executeQuery(Object[] parameters) {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
    String preparedQuery = prepareQuery(parameters);
    Query query = new Query(preparedQuery);
    query.returnFields(returnFields);

    Optional<Pageable> maybePageable = Optional.empty();

    if (queryMethod.isPageQuery()) {
      maybePageable = Arrays.stream(parameters).filter(o -> o instanceof Pageable).map(o -> (Pageable) o).findFirst();

      if (maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        if (!pageable.isUnpaged()) {
          query.limit(Long.valueOf(pageable.getOffset()).intValue(), pageable.getPageSize());
  
          if (pageable.getSort() != null) {
            for (Order order : pageable.getSort()) {
              query.setSortBy(order.getProperty(), order.isAscending());
            }
          }
        }
      }
    }

    // Intercept TAG collection queries with empty parameters and use an
    // aggregation
    if (queryMethod.isCollectionQuery() && !queryMethod.getParameters().isEmpty()) {
      List<Collection<?>> emptyCollectionParams = Arrays.asList(parameters).stream() //
          .filter(p -> p instanceof Collection) //
          .map(p -> (Collection<?>) p) //
          .filter(c -> c.isEmpty()) //
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
      List<Object> content = searchResult.docs.stream() //
          .map(d -> ObjectUtils.documentToObject(d, queryMethod.getReturnedObjectType(), mappingConverter)) //
          .collect(Collectors.toList());

      if (maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        result = new PageImpl<>(content, pageable, searchResult.totalResults);
      }

    } else if (queryMethod.isQueryForEntity() && !queryMethod.isCollectionQuery()) {
      if (searchResult.totalResults > 0 && !searchResult.docs.isEmpty()) {
        result = ObjectUtils.documentToObject(searchResult.docs.get(0), queryMethod.getReturnedObjectType(),
            mappingConverter);
      } else {
        result = null;
      }
    } else if (queryMethod.isQueryForEntity() && queryMethod.isCollectionQuery()) {
      result = searchResult.docs.stream() //
          .map(d -> ObjectUtils.documentToObject(d, queryMethod.getReturnedObjectType(), mappingConverter)) //
          .collect(Collectors.toList());
    }

    return result;
  }

  private Object executeAggregation(Object[] parameters) {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
    AggregationBuilder aggregation = new AggregationBuilder(value).load(load);
    for (Map.Entry<String, String> entry : apply.entrySet()) {
      aggregation.apply(entry.getKey(), entry.getValue());
    }
    AggregationResult aggregationResult = ops.aggregate(aggregation);

    // what to return
    Object result = null;
    if (queryMethod.getReturnedObjectType() == AggregationResult.class) {
      result = aggregationResult;
    } else if (queryMethod.isCollectionQuery()) {
      result = Collections.emptyList();
    }

    return result;
  }

  private Object executeFtTagVals() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);

    return ops.tagVals(this.value);
  }

  private String prepareQuery(final Object[] parameters) {
    logger.debug(String.format("parameters: %s", Arrays.toString(parameters)));
    List<Object> params = new ArrayList<>(Arrays.asList(parameters));
    StringBuilder preparedQuery = new StringBuilder();

    boolean multipleOrParts = queryOrParts.size() > 1;
    logger.debug(String.format("queryOrParts: %s", queryOrParts.size()));
    if (!queryOrParts.isEmpty()) {
      preparedQuery.append(queryOrParts.stream().map(qop -> {
        String orPart = multipleOrParts ? "(" : "";
        orPart = orPart + qop.stream().map(fieldClauses -> {
          String fieldName = fieldClauses.getFirst();
          QueryClause queryClause = fieldClauses.getSecond();
          int paramsCnt = queryClause.getValue().getNumberOfArguments();

          Object[] ps = params.subList(0, paramsCnt).toArray();
          params.subList(0, paramsCnt).clear();

          return queryClause.prepareQuery(fieldName, ps);
        }).collect(Collectors.joining(" "));
        orPart = orPart + (multipleOrParts ? ")" : "");

        return orPart;
      }).collect(Collectors.joining(" | ")));
    } else {
      @SuppressWarnings("unchecked")
      Iterator<Parameter> iterator = (Iterator<Parameter>) queryMethod.getParameters().iterator();
      int index = 0;
      
      if (value != null && !value.isBlank()) {
        preparedQuery.append(value);
      }

      while (iterator.hasNext()) {
        Parameter p = iterator.next();
        String key = (p.getName().isPresent() ? p.getName().get()
            : (paramNames.size() > index ? paramNames.get(index) : ""));
        if (!key.isBlank()) {
          String v = "";
  
          if (parameters[index] instanceof Collection<?>) {
            @SuppressWarnings("rawtypes")
            Collection<?> c = (Collection) parameters[index];
            v = c.stream().map(n -> ObjectUtils.asString(n, mappingConverter)).collect(Collectors.joining(" | "));
          } else {
            v = ObjectUtils.asString(parameters[index], mappingConverter);
          }
  
          preparedQuery = new StringBuilder(preparedQuery.toString().replace("$" + key, v));          
        }
        index++;
      }
    }

    return preparedQuery.toString();
  }

  private Map<String, String> splitApplyArguments(String... entries) {
    return IntStream //
        .range(0, entries.length / 2) //
        .map(i -> i * 2) //
        .collect(HashMap::new, (m, i) -> m.put(entries[i], entries[i + 1]), Map::putAll);
  }

}