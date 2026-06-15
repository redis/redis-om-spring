package com.redis.om.spring.repository.query;

import java.lang.reflect.Field;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.*;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.util.Pair;
import org.springframework.util.ReflectionUtils;

import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteQueryExecutor;
import com.redis.om.spring.repository.query.bloom.BloomQueryExecutor;
import com.redis.om.spring.repository.query.clause.QueryClause;
import com.redis.om.spring.repository.query.countmin.CountMinQueryExecutor;
import com.redis.om.spring.repository.query.cuckoo.CuckooQueryExecutor;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.aggr.*;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;

/**
 * Abstract base class shared by {@link RediSearchQuery} (JSON/Document) and
 * {@link RedisEnhancedQuery} (Hash). Holds the shared state and provides
 * concrete implementations of all execution methods that do not differ between
 * the two storage models.
 *
 * <p>Subclasses must implement the four abstract methods that are storage-model-specific:
 * {@link #executeQuery}, {@link #executeDeleteQuery}, {@link #executeNullQuery},
 * and {@link #prepareQuery}.
 */
public abstract class AbstractRedisQuery implements RepositoryQuery {

  protected static final Log logger = LogFactory.getLog(AbstractRedisQuery.class);

  // --- Shared final state ---
  protected final QueryMethod queryMethod;
  protected final RedisOMProperties redisOMProperties;
  protected final Class<?> domainType;
  protected final RedisModulesOperations<String> modulesOperations;
  protected final RediSearchIndexer indexer;
  protected final EntityStream entityStream;
  protected final BloomQueryExecutor bloomQueryExecutor;
  protected final CuckooQueryExecutor cuckooQueryExecutor;
  protected final CountMinQueryExecutor countMinQueryExecutor;
  protected final AutoCompleteQueryExecutor autoCompleteQueryExecutor;

  // --- Shared mutable final collections ---
  protected final List<Entry<String, String>> aggregationLoad = new ArrayList<>();
  protected final List<Entry<String, String>> aggregationApply = new ArrayList<>();
  protected final List<Group> aggregationGroups = new ArrayList<>();
  protected final List<SortedField> aggregationSortedFields = new ArrayList<>();
  protected final List<List<Pair<String, QueryClause>>> queryOrParts = new ArrayList<>();
  protected final List<String> paramNames = new ArrayList<>();

  // --- Shared mutable state (set during initFromMethod) ---
  protected boolean hasLanguageParameter;
  protected boolean isANDQuery;
  protected RediSearchQueryType type;
  protected String value;
  protected String[] returnFields;
  protected Integer offset;
  protected Integer limit;
  protected String sortBy;
  protected Boolean sortAscending;
  protected String[] aggregationFilter;
  protected Integer aggregationSortByMax;
  protected Long aggregationTimeout;
  protected Boolean aggregationVerbatim;
  protected boolean isNullParamQuery;
  protected Dialect dialect;

  /**
   * Base constructor. Sets the fields that are known at construction time.
   * Subclasses must call {@link #initFromMethod} after their own field assignments
   * to complete query-type detection and annotation parsing.
   */
  protected AbstractRedisQuery(QueryMethod queryMethod, RedisModulesOperations<String> modulesOperations,
      RediSearchIndexer indexer, EntityStream entityStream, RedisOMProperties redisOMProperties,
      Dialect defaultDialect) {
    this.queryMethod = queryMethod;
    this.modulesOperations = modulesOperations;
    this.indexer = indexer;
    this.entityStream = entityStream;
    this.redisOMProperties = redisOMProperties;
    this.domainType = queryMethod.getEntityInformation().getJavaType();
    this.dialect = defaultDialect;
    this.bloomQueryExecutor = new BloomQueryExecutor(this, modulesOperations);
    this.cuckooQueryExecutor = new CuckooQueryExecutor(this, modulesOperations);
    this.countMinQueryExecutor = new CountMinQueryExecutor(this, modulesOperations);
    this.autoCompleteQueryExecutor = new AutoCompleteQueryExecutor(this, modulesOperations);
  }

  // ---------------------------------------------------------------------------
  // Annotation / method-name processing (called explicitly by subclass constructors)
  // ---------------------------------------------------------------------------

  /**
   * Parses the repository method's annotations and name to determine the query type
   * and populate shared state. Must be called by each subclass constructor after all
   * subclass-specific fields have been assigned.
   */
  @SuppressWarnings(
    { "unchecked", "rawtypes" }
  )
  protected void initFromMethod(Class<?> repoClass, RepositoryMetadata metadata, Class[] params, String methodName) {
    this.hasLanguageParameter = Arrays.stream(params).anyMatch(c -> c.isAssignableFrom(SearchLanguage.class));
    this.isANDQuery = QueryClause.hasContainingAllClause(queryMethod.getName());
    if (this.isANDQuery) {
      methodName = QueryClause.getPostProcessMethodName(methodName);
    }

    try {
      java.lang.reflect.Method method = ReflectionUtils.findMethod(repoClass, queryMethod.getName(), params);
      if (method == null) {
        return;
      }

      if (method.isAnnotationPresent(UseDialect.class)) {
        this.dialect = method.getAnnotation(UseDialect.class).dialect();
      }

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
        Arrays.stream(aggregation.load()).forEach(load -> aggregationLoad.add(new SimpleEntry<>(load.property(), load
            .alias())));
        Arrays.stream(aggregation.apply()).forEach(apply -> aggregationApply.add(new SimpleEntry<>(apply.alias(), apply
            .expression())));
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
                  r = Reducers.first_value(arg0, new SortedField(arg1, order));
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
      } else if (handleSpecialQueryMethod(methodName, metadata)) {
        // handled by subclass (e.g. MapContains in RediSearchQuery)
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
    } catch (Exception e) {
      logger.debug(String.format("Could not resolve query method %s: %s", queryMethod.getName(), e.getMessage()));
    }
  }

  /**
   * Hook for subclasses to intercept query method handling before the default
   * PartTree branch. Return {@code true} if the subclass handled the method
   * (in which case the PartTree branch is skipped), {@code false} otherwise.
   *
   * <p>Note: this is called from {@link #initFromMethod}, which runs from the
   * subclass constructor — all subclass fields are guaranteed to be assigned
   * before this method is invoked.
   */
  protected boolean handleSpecialQueryMethod(String methodName, RepositoryMetadata metadata) {
    return false;
  }

  // ---------------------------------------------------------------------------
  // Abstract methods — storage-model-specific
  // ---------------------------------------------------------------------------

  protected abstract Object executeQuery(Object[] parameters);

  protected abstract Object executeDeleteQuery(Object[] parameters);

  protected abstract Object executeNullQuery(Object[] parameters);

  protected abstract String prepareQuery(Object[] parameters, boolean excludeNullParams);

  // ---------------------------------------------------------------------------
  // RepositoryQuery interface
  // ---------------------------------------------------------------------------

  @Override
  public Object execute(Object[] parameters) {
    Optional<String> maybeBloomFilter = bloomQueryExecutor.getBloomFilter();
    Optional<String> maybeCuckooFilter = cuckooQueryExecutor.getCuckooFilter();
    Optional<String> maybeCountMinSketch = countMinQueryExecutor.getCountMinSketch();

    if (maybeBloomFilter.isPresent()) {
      return bloomQueryExecutor.executeBloomQuery(parameters, maybeBloomFilter.get());
    } else if (maybeCuckooFilter.isPresent()) {
      return cuckooQueryExecutor.executeCuckooQuery(parameters, maybeCuckooFilter.get());
    } else if (maybeCountMinSketch.isPresent()) {
      return countMinQueryExecutor.executeCountMinQuery(parameters, maybeCountMinSketch.get());
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

  // ---------------------------------------------------------------------------
  // Shared parsing
  // ---------------------------------------------------------------------------

  protected void processPartTree(PartTree pt, List<String> nullParamNames, List<String> notNullParamNames) {
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

    Optional<Order> maybeOrder = pt.getSort().stream().findFirst();
    if (maybeOrder.isPresent()) {
      Order order = maybeOrder.get();
      sortBy = order.getProperty();
      sortAscending = order.isAscending();
    }

    if (pt.isLimiting()) {
      this.limit = pt.getMaxResults();
    }
  }

  protected List<Pair<String, QueryClause>> extractQueryFields(Class<?> type, Part part, List<PropertyPath> path) {
    return extractQueryFields(type, part, path, 0);
  }

  /**
   * Maps an annotated entity field to a list of (fieldName, QueryClause) pairs
   * for use in RediSearch query construction. Subclasses may override to add
   * storage-model-specific handling (e.g. lexicographic clauses in
   * {@link RediSearchQuery}).
   */
  protected List<Pair<String, QueryClause>> extractQueryFields(Class<?> type, Part part, List<PropertyPath> path,
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
      qf.add(Pair.of(actualKey, QueryClause.get(redis.clients.jedis.search.Schema.FieldType.TEXT, part.getType())));
    } else if (field.isAnnotationPresent(Searchable.class)) {
      Searchable indexAnnotation = field.getAnnotation(Searchable.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      qf.add(Pair.of(actualKey, resolveQueryClause(redis.clients.jedis.search.Schema.FieldType.TEXT, part.getType(),
          indexAnnotation.lexicographic())));
    } else if (field.isAnnotationPresent(TagIndexed.class)) {
      TagIndexed indexAnnotation = field.getAnnotation(TagIndexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      qf.add(Pair.of(actualKey, QueryClause.get(redis.clients.jedis.search.Schema.FieldType.TAG, part.getType())));
    } else if (field.isAnnotationPresent(GeoIndexed.class)) {
      GeoIndexed indexAnnotation = field.getAnnotation(GeoIndexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      qf.add(Pair.of(actualKey, QueryClause.get(redis.clients.jedis.search.Schema.FieldType.GEO, part.getType())));
    } else if (field.isAnnotationPresent(NumericIndexed.class)) {
      NumericIndexed indexAnnotation = field.getAnnotation(NumericIndexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      qf.add(Pair.of(actualKey, QueryClause.get(redis.clients.jedis.search.Schema.FieldType.NUMERIC, part.getType())));
    } else if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class)) {
      Class<?> fieldType = org.springframework.util.ClassUtils.resolvePrimitiveIfNecessary(field.getType());
      if (Number.class.isAssignableFrom(fieldType)) {
        qf.add(Pair.of(key, QueryClause.get(redis.clients.jedis.search.Schema.FieldType.NUMERIC, part.getType())));
      } else {
        qf.add(Pair.of(key, QueryClause.get(redis.clients.jedis.search.Schema.FieldType.TAG, part.getType())));
      }
    } else if (field.isAnnotationPresent(Indexed.class)) {
      Indexed indexAnnotation = field.getAnnotation(Indexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      Class<?> fieldType = org.springframework.util.ClassUtils.resolvePrimitiveIfNecessary(field.getType());
      qf.addAll(extractIndexedQueryFields(field, fieldType, actualKey, key, part, path, level, indexAnnotation));
    }

    return qf;
  }

  /**
   * Handles {@code @Indexed} fields in {@link #extractQueryFields}. Subclasses
   * (e.g. {@link RediSearchQuery}) override this to add lexicographic, nested,
   * and Map field handling.
   */
  protected List<Pair<String, QueryClause>> extractIndexedQueryFields(Field field, Class<?> fieldType, String actualKey,
      String key, Part part, List<PropertyPath> path, int level, Indexed indexAnnotation) {
    List<Pair<String, QueryClause>> qf = new ArrayList<>();
    if (CharSequence.class.isAssignableFrom(
        fieldType) || (fieldType == Boolean.class) || (fieldType == UUID.class) || (fieldType.isEnum())) {
      qf.add(Pair.of(actualKey, resolveQueryClause(redis.clients.jedis.search.Schema.FieldType.TAG, part.getType(),
          indexAnnotation.lexicographic())));
    } else if (Number.class.isAssignableFrom(
        fieldType) || (fieldType == java.time.LocalDateTime.class) || (fieldType == java.time.LocalDate.class) || (fieldType == java.util.Date.class) || (fieldType == java.time.Instant.class) || (fieldType == java.time.OffsetDateTime.class)) {
      qf.add(Pair.of(actualKey, QueryClause.get(redis.clients.jedis.search.Schema.FieldType.NUMERIC, part.getType())));
    } else if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)) {
      Optional<Class<?>> maybeCollectionType = com.redis.om.spring.util.ObjectUtils.getCollectionElementClass(field);
      if (maybeCollectionType.isPresent()) {
        Class<?> collectionType = maybeCollectionType.get();
        if (Number.class.isAssignableFrom(collectionType)) {
          qf.add(Pair.of(actualKey, isANDQuery ?
              QueryClause.NUMERIC_CONTAINING_ALL :
              QueryClause.get(redis.clients.jedis.search.Schema.FieldType.NUMERIC, part.getType())));
        } else if (collectionType == org.springframework.data.geo.Point.class) {
          qf.add(Pair.of(actualKey, isANDQuery ?
              QueryClause.GEO_CONTAINING_ALL :
              QueryClause.get(redis.clients.jedis.search.Schema.FieldType.GEO, part.getType())));
        } else {
          qf.add(Pair.of(actualKey, isANDQuery ?
              QueryClause.TAG_CONTAINING_ALL :
              QueryClause.get(redis.clients.jedis.search.Schema.FieldType.TAG, part.getType())));
        }
      }
    } else if (fieldType == org.springframework.data.geo.Point.class) {
      qf.add(Pair.of(actualKey, QueryClause.get(redis.clients.jedis.search.Schema.FieldType.GEO, part.getType())));
    } else {
      qf.addAll(extractQueryFields(fieldType, part, path, level + 1));
    }
    return qf;
  }

  /**
   * Resolves the query clause for a field type and part type. The default
   * implementation delegates to {@link QueryClause#get}. {@link RediSearchQuery}
   * overrides this to return lexicographic clauses when applicable.
   */
  protected QueryClause resolveQueryClause(redis.clients.jedis.search.Schema.FieldType fieldType, Part.Type partType,
      boolean lexicographic) {
    return QueryClause.get(fieldType, partType);
  }

  // ---------------------------------------------------------------------------
  // Shared execution
  // ---------------------------------------------------------------------------

  protected Object executeAggregation(Object[] parameters) {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);

    String preparedQuery = prepareQuery(parameters, true);
    AggregationBuilder aggregation = new AggregationBuilder(preparedQuery);

    if (aggregationTimeout != null) {
      aggregation.timeout(aggregationTimeout);
    }
    if (aggregationVerbatim != null) {
      aggregation.verbatim();
    }

    for (Map.Entry<String, String> apply : aggregationLoad) {
      if (apply.getValue().isBlank()) {
        aggregation.load(apply.getKey());
      } else {
        aggregation.load(apply.getKey(), "AS", apply.getValue());
      }
    }

    aggregationGroups.forEach(aggregation::groupBy);

    if (aggregationFilter != null) {
      for (String filter : aggregationFilter) {
        aggregation.filter(filter);
      }
    }

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

    if (sortBy != null && !sortBy.isBlank()) {
      aggregation.sortByAsc(indexer.getAlias(domainType, sortBy));
    } else if (!aggregationSortedFields.isEmpty()) {
      if (aggregationSortByMax != null) {
        aggregation.sortBy(aggregationSortByMax, aggregationSortedFields.toArray(new SortedField[] {}));
      } else {
        aggregation.sortBy(aggregationSortedFields.toArray(new SortedField[] {}));
      }
    }

    for (Map.Entry<String, String> apply : aggregationApply) {
      aggregation.apply(apply.getValue(), apply.getKey());
    }

    if (needsLimit) {
      if ((limit != null) || (offset != null)) {
        aggregation.limit(offset != null ? offset : 0, limit != null ? limit : 0);
      } else {
        aggregation.limit(0, redisOMProperties.getRepository().getQuery().getLimit());
      }
    }

    aggregation.dialect(dialect.getValue());

    AggregationResult aggregationResult = ops.aggregate(aggregation);

    Object result = null;
    if (queryMethod.getReturnedObjectType() == AggregationResult.class) {
      result = aggregationResult;
    } else if (queryMethod.getReturnedObjectType() == Map.class) {
      List<?> content = aggregationResult.getResults().stream().map(m -> m.entrySet().stream().map(
          e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue() != null ? e.getValue().toString() : "")).collect(
              Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))).collect(Collectors.toList());
      if (queryMethod.isPageQuery() && maybePageable.isPresent()) {
        result = new PageImpl<>(content, maybePageable.get(), aggregationResult.getTotalResults());
      }
    }

    return result;
  }

  protected Object executeFtTagVals() {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    return ops.tagVals(this.value);
  }

  protected boolean hasIndexMissing(String fieldName) {
    try {
      Field field = ReflectionUtils.findField(domainType, fieldName);
      if (field == null) {
        return false;
      }
      if (field.isAnnotationPresent(Indexed.class)) {
        return field.getAnnotation(Indexed.class).indexMissing();
      }
      if (field.isAnnotationPresent(Searchable.class)) {
        return field.getAnnotation(Searchable.class).indexMissing();
      }
      return false;
    } catch (Exception e) {
      logger.debug("Failed to check indexMissing for field: " + fieldName, e);
      return false;
    }
  }
}
