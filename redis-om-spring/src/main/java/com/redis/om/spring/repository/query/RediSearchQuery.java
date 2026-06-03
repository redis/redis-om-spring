package com.redis.om.spring.repository.query;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.core.PropertyPath;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.geo.Point;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.*;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.util.Pair;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.clause.QueryClause;
import com.redis.om.spring.repository.query.lexicographic.LexicographicQueryExecutor;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Schema.FieldType;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.*;
import redis.clients.jedis.util.SafeEncoder;

/**
 * A Spring Data repository query implementation that executes queries against Redis using RediSearch.
 * This class is the core query execution engine for Redis OM Spring, supporting various query types
 * including full-text search, aggregations, probabilistic data structure operations, and autocomplete.
 *
 * <p>RediSearchQuery analyzes repository method signatures and annotations to determine the appropriate
 * query strategy and generates optimized RediSearch queries. It supports both declarative query methods
 * (using method naming conventions) and explicit queries (using {@code @Query} and {@code @Aggregation}
 * annotations).
 *
 * <p>Supported query types:
 * <ul>
 * <li><strong>Search queries</strong> - Full-text and field-based searches using RediSearch syntax</li>
 * <li><strong>Aggregation queries</strong> - Complex data processing and grouping operations</li>
 * <li><strong>Delete queries</strong> - Bulk deletion operations based on search criteria</li>
 * <li><strong>Tag value queries</strong> - Retrieving distinct values from tag fields</li>
 * <li><strong>Autocomplete queries</strong> - Suggestion-based search functionality</li>
 * <li><strong>Probabilistic queries</strong> - Bloom filter, Cuckoo filter, and Count-Min Sketch operations</li>
 * </ul>
 *
 * @author Redis OM Spring Team
 * @see RepositoryQuery
 * @see QueryMethod
 * @see RediSearchIndexer
 * @see RedisModulesOperations
 * @since 1.0
 */
public class RediSearchQuery extends AbstractRedisQuery {

  /**
   * Set of QueryClause types that represent lexicographic operations.
   */
  private static final Set<QueryClause> LEXICOGRAPHIC_QUERY_CLAUSES = Set.of(QueryClause.TEXT_GREATER_THAN,
      QueryClause.TEXT_LESS_THAN, QueryClause.TEXT_GREATER_THAN_EQUAL, QueryClause.TEXT_LESS_THAN_EQUAL,
      QueryClause.TEXT_BETWEEN, QueryClause.TAG_GREATER_THAN, QueryClause.TAG_LESS_THAN,
      QueryClause.TAG_GREATER_THAN_EQUAL, QueryClause.TAG_LESS_THAN_EQUAL, QueryClause.TAG_BETWEEN);

  /**
   * Set of Part.Type values that represent lexicographic operations.
   */
  private static final Set<Part.Type> LEXICOGRAPHIC_PART_TYPES = Set.of(Part.Type.GREATER_THAN, Part.Type.LESS_THAN,
      Part.Type.GREATER_THAN_EQUAL, Part.Type.LESS_THAN_EQUAL, Part.Type.BETWEEN);

  /**
   * Determines the Redis field type for a given Java class.
   */
  private static FieldType getRedisFieldType(Class<?> fieldType) {
    if (CharSequence.class.isAssignableFrom(
        fieldType) || fieldType == Boolean.class || fieldType == UUID.class || fieldType == Ulid.class || fieldType
            .isEnum()) {
      return FieldType.TAG;
    } else if (Number.class.isAssignableFrom(
        fieldType) || fieldType == LocalDateTime.class || fieldType == LocalDate.class || fieldType == Date.class || fieldType == Instant.class || fieldType == OffsetDateTime.class) {
      return FieldType.NUMERIC;
    } else if (fieldType == Point.class) {
      return FieldType.GEO;
    } else {
      return null;
    }
  }

  /**
   * Gets the Redis field type for Map value types.
   * This differs from getRedisFieldType() for Boolean values - Map Boolean values
   * are indexed as NUMERIC fields (serialized as 1/0) rather than TAG fields.
   */
  private static FieldType getRedisFieldTypeForMapValue(Class<?> fieldType) {
    if (CharSequence.class.isAssignableFrom(
        fieldType) || fieldType == UUID.class || fieldType == Ulid.class || fieldType.isEnum()) {
      return FieldType.TAG;
    } else if (Number.class.isAssignableFrom(
        fieldType) || fieldType == Boolean.class || fieldType == LocalDateTime.class || fieldType == LocalDate.class || fieldType == Date.class || fieldType == Instant.class || fieldType == OffsetDateTime.class) {
      return FieldType.NUMERIC;
    } else if (fieldType == Point.class || "org.springframework.data.geo.Point".equals(fieldType.getName())) {
      return FieldType.GEO;
    } else {
      return null;
    }
  }

  // RSQ-specific fields
  private final GsonBuilder gsonBuilder;
  private final LexicographicQueryExecutor lexicographicQueryExecutor;
  private boolean isMapContainsQuery;
  private Gson gson;

  /**
   * Creates a new RediSearchQuery instance for the given repository method.
   */
  @SuppressWarnings(
    "unchecked"
  )
  public RediSearchQuery(//
      QueryMethod queryMethod, //
      RepositoryMetadata metadata, //
      RediSearchIndexer indexer, //
      org.springframework.data.repository.query.ValueExpressionDelegate valueExpressionDelegate, //
      KeyValueOperations keyValueOperations, //
      RedisModulesOperations<?> rmo, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
      GsonBuilder gsonBuilder, //
      RedisOMProperties redisOMProperties //
  ) {
    super(queryMethod, (RedisModulesOperations<String>) rmo, indexer, new EntityStreamImpl(
        (RedisModulesOperations<String>) rmo, gsonBuilder, indexer), redisOMProperties, Dialect.TWO);

    logger.info(String.format("Creating %s query method", queryMethod.getName()));

    this.gsonBuilder = gsonBuilder;
    this.lexicographicQueryExecutor = new LexicographicQueryExecutor(this, (RedisModulesOperations<String>) rmo,
        indexer);

    // Only detect nested MapContains patterns (e.g., positionsMapContainsCusip)
    // Simple MapContains (e.g., stringValuesMapContains) should use normal processing
    this.isMapContainsQuery = QueryClause.hasMapContainsNestedClause(queryMethod.getName());

    Class<?> repoClass = metadata.getRepositoryInterface();
    @SuppressWarnings(
      "rawtypes"
    ) Class[] params = queryMethod.getParameters().stream().map(Parameter::getType).toArray(Class[]::new);

    // Compute methodName: apply AND processing and simple MapContains name processing
    String methodName = queryMethod.getName();
    if (QueryClause.hasContainingAllClause(methodName)) {
      methodName = QueryClause.getPostProcessMethodName(methodName);
    }
    // Process simple MapContains patterns (e.g., stringValuesMapContains -> stringValues)
    // for PartTree parsing, but not nested patterns (those are handled by processMapContainsQuery)
    if (QueryClause.hasMapContainsClause(methodName) && !QueryClause.hasMapContainsNestedClause(methodName)) {
      methodName = QueryClause.processMapContainsMethodName(methodName);
    }

    super.initFromMethod(repoClass, metadata, params, methodName);
  }

  // ---------------------------------------------------------------------------
  // Overrides
  // ---------------------------------------------------------------------------

  @Override
  protected boolean handleSpecialQueryMethod(String methodName, RepositoryMetadata metadata) {
    if (this.isMapContainsQuery) {
      this.type = queryMethod.getName().matches("(?:remove|delete).*") ?
          RediSearchQueryType.DELETE :
          RediSearchQueryType.QUERY;
      this.returnFields = new String[] {};
      // Apply MapContains name processing to the already-AND-processed methodName
      String processedName = methodName;
      if (QueryClause.hasMapContainsClause(processedName) && !QueryClause.hasMapContainsNestedClause(processedName)) {
        processedName = QueryClause.processMapContainsMethodName(processedName);
      }
      processMapContainsQuery(processedName);
      return true;
    }
    return false;
  }

  @Override
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
      qf.add(Pair.of(actualKey, QueryClause.get(FieldType.TEXT, part.getType())));
    } else if (field.isAnnotationPresent(Searchable.class)) {
      Searchable indexAnnotation = field.getAnnotation(Searchable.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      QueryClause clause;
      if (indexAnnotation.lexicographic() && isLexicographicPartType(part.getType())) {
        clause = getLexicographicQueryClause(FieldType.TEXT, part.getType());
      } else {
        clause = QueryClause.get(FieldType.TEXT, part.getType());
      }
      logger.debug(String.format("Searchable field %s: lexicographic=%s, partType=%s, clause=%s", actualKey,
          indexAnnotation.lexicographic(), part.getType(), clause));
      qf.add(Pair.of(actualKey, clause));
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
    } else if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class)) {
      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());
      FieldType redisFieldType = getRedisFieldType(fieldType);
      if (redisFieldType == FieldType.NUMERIC) {
        qf.add(Pair.of(key, QueryClause.get(FieldType.NUMERIC, part.getType())));
      } else {
        qf.add(Pair.of(key, QueryClause.get(FieldType.TAG, part.getType())));
      }
    } else if (field.isAnnotationPresent(Indexed.class)) {
      Indexed indexAnnotation = field.getAnnotation(Indexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());
      qf.addAll(extractIndexedQueryFields(field, fieldType, actualKey, key, part, path, level, indexAnnotation));
    }

    return qf;
  }

  @Override
  protected List<Pair<String, QueryClause>> extractIndexedQueryFields(Field field, Class<?> fieldType, String actualKey,
      String key, Part part, List<PropertyPath> path, int level, Indexed indexAnnotation) {
    List<Pair<String, QueryClause>> qf = new ArrayList<>();
    FieldType redisFieldType = getRedisFieldType(fieldType);

    if (redisFieldType == FieldType.TAG) {
      QueryClause clause;
      if (indexAnnotation.lexicographic() && isLexicographicPartType(part.getType())) {
        clause = getLexicographicQueryClause(FieldType.TAG, part.getType());
      } else {
        clause = QueryClause.get(FieldType.TAG, part.getType());
      }
      logger.debug(String.format("Indexed TAG field %s: lexicographic=%s, partType=%s, clause=%s", actualKey,
          indexAnnotation.lexicographic(), part.getType(), clause));
      qf.add(Pair.of(actualKey, clause));
    } else if (redisFieldType == FieldType.NUMERIC) {
      qf.add(Pair.of(actualKey, QueryClause.get(FieldType.NUMERIC, part.getType())));
    } else if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)) {
      Optional<Class<?>> maybeCollectionType = ObjectUtils.getCollectionElementClass(field);
      if (maybeCollectionType.isPresent()) {
        Class<?> collectionType = maybeCollectionType.get();

        if (indexAnnotation.schemaFieldType() == SchemaFieldType.NESTED) {
          String nestedFieldName = path.size() > level + 1 ? path.get(level + 1).getSegment() : "";
          if (!nestedFieldName.isEmpty()) {
            String nestedKey = field.getName() + "_" + nestedFieldName;
            logger.debug(String.format("Processing nested array field query: %s -> %s", key, nestedKey));
            Field nestedField = ReflectionUtils.findField(collectionType, nestedFieldName);
            if (nestedField != null) {
              String alias = QueryUtils.searchIndexFieldAliasFor(nestedField, field.getName());
              String actualNestedKey = (alias != null && !alias.isEmpty()) ? alias : nestedKey;
              Class<?> nestedFieldType = ClassUtils.resolvePrimitiveIfNecessary(nestedField.getType());
              FieldType nestedRedisFieldType = getRedisFieldType(nestedFieldType);
              if (nestedRedisFieldType != null) {
                qf.add(Pair.of(actualNestedKey, QueryClause.get(nestedRedisFieldType, part.getType())));
              }
            }
          }
        } else if (Number.class.isAssignableFrom(collectionType)) {
          qf.add(Pair.of(actualKey, isANDQuery ?
              QueryClause.NUMERIC_CONTAINING_ALL :
              QueryClause.get(FieldType.NUMERIC, part.getType())));
        } else if (collectionType == Point.class) {
          qf.add(Pair.of(actualKey, isANDQuery ?
              QueryClause.GEO_CONTAINING_ALL :
              QueryClause.get(FieldType.GEO, part.getType())));
        } else if (getRedisFieldType(collectionType) == FieldType.TAG) {
          qf.add(Pair.of(actualKey, isANDQuery ?
              QueryClause.TAG_CONTAINING_ALL :
              QueryClause.get(FieldType.TAG, part.getType())));
        } else {
          qf.addAll(extractQueryFields(collectionType, part, path, level + 1));
        }
      }
    } else if (Map.class.isAssignableFrom(fieldType)) {
      String mapValueKey = key + "_values";
      Optional<Class<?>> maybeValueType = ObjectUtils.getMapValueClass(field);
      if (maybeValueType.isPresent()) {
        Class<?> valueType = maybeValueType.get();
        FieldType valueFieldType = getRedisFieldTypeForMapValue(valueType);
        if (valueFieldType != null) {
          qf.add(Pair.of(mapValueKey, QueryClause.get(valueFieldType, part.getType())));
        }
      }
    } else if (redisFieldType == FieldType.GEO) {
      qf.add(Pair.of(actualKey, QueryClause.get(FieldType.GEO, part.getType())));
    } else {
      qf.addAll(extractQueryFields(fieldType, part, path, level + 1));
    }

    return qf;
  }

  @Override
  protected QueryClause resolveQueryClause(redis.clients.jedis.search.Schema.FieldType fieldType, Part.Type partType,
      boolean lexicographic) {
    if (lexicographic && isLexicographicPartType(partType)) {
      return getLexicographicQueryClause(fieldType, partType);
    }
    return QueryClause.get(fieldType, partType);
  }

  // ---------------------------------------------------------------------------
  // Storage-model-specific implementations
  // ---------------------------------------------------------------------------

  @Override
  protected Object executeQuery(Object[] parameters) {
    ParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);
    ResultProcessor processor = queryMethod.getResultProcessor().withDynamicProjection(accessor);

    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    boolean excludeNullParams = !isNullParamQuery;

    // Check if all query parts are lexicographic
    boolean allLexicographic = true;
    for (List<Pair<String, QueryClause>> orPartParts : queryOrParts) {
      for (Pair<String, QueryClause> pair : orPartParts) {
        QueryClause clause = pair.getSecond();
        logger.debug(String.format("Checking clause: %s, is lexicographic: %s", clause, isLexicographicClause(clause)));
        if (!isLexicographicClause(clause)) {
          allLexicographic = false;
          break;
        }
      }
    }

    String preparedQuery;
    if (allLexicographic && !queryOrParts.isEmpty()) {
      logger.debug("Processing as lexicographic query");
      preparedQuery = lexicographicQueryExecutor.processLexicographicQuery(queryOrParts, parameters, domainType);
      logger.debug(String.format("Lexicographic query returned: %s", preparedQuery));
      if (preparedQuery == null) {
        preparedQuery = "*";
      }
    } else {
      preparedQuery = prepareQuery(parameters, excludeNullParams);
    }

    Query query = new Query(preparedQuery);
    query.dialect(2);

    ReturnedType returnedType = processor.getReturnedType();

    boolean isProjecting = returnedType.isProjecting() && returnedType.getReturnedType() != SearchResult.class;
    boolean isOpenProjecting = Arrays.stream(returnedType.getReturnedType().getMethods()).anyMatch(m -> m
        .isAnnotationPresent(Value.class));
    boolean canPerformQueryOptimization = isProjecting && !isOpenProjecting;

    if (canPerformQueryOptimization) {
      query.returnFields(returnedType.getInputProperties().stream().map(inputProperty -> new FieldName(
          "$." + inputProperty, inputProperty)).toArray(FieldName[]::new));
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
        query.limit(offset != null ? offset : 0, limit != null ?
            limit :
            redisOMProperties.getRepository().getQuery().getLimit());
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

    if (queryMethod.isCollectionQuery() && !queryMethod.getParameters().isEmpty()) {
      List<java.util.Collection<?>> emptyCollectionParams = Arrays.stream(parameters) //
          .filter(java.util.Collection.class::isInstance) //
          .map(p -> (java.util.Collection<?>) p) //
          .filter(java.util.Collection::isEmpty) //
          .collect(Collectors.toList());
      if (!emptyCollectionParams.isEmpty()) {
        return Collections.emptyList();
      }
    }

    query.dialect(dialect.getValue());

    SearchResult searchResult = ops.search(query);

    Object result = null;

    if (SearchStream.class.isAssignableFrom(queryMethod.getReturnedObjectType())) {
      @SuppressWarnings(
        "unchecked"
      ) SearchStream<?> stream = entityStream.of((Class<Object>) domainType);
      String queryString = prepareQuery(parameters, true);
      if (!queryString.equals("*") && !queryString.isEmpty()) {
        stream = stream.filter(queryString);
      }
      if (limit != null && limit > 0) {
        stream = stream.limit(limit);
      }
      return stream;
    } else if (processor.getReturnedType().getReturnedType() == boolean.class || processor.getReturnedType()
        .getReturnedType() == Boolean.class) {
      result = searchResult.getTotalResults() > 0;
    } else if (queryMethod.getReturnedObjectType() == SearchResult.class) {
      result = searchResult;
    } else if (queryMethod.isPageQuery()) {
      List<Object> content = searchResult.getDocuments().stream().map(this::parseDocumentResult).toList();
      if (maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        result = new PageImpl<>(content, pageable, searchResult.getTotalResults());
      }
    } else if (!queryMethod.isCollectionQuery()) {
      if (!searchResult.getDocuments().isEmpty()) {
        redis.clients.jedis.search.Document doc = searchResult.getDocuments().get(0);
        result = parseDocumentResult(doc);
      }
    } else if ((queryMethod.isCollectionQuery()) || this.type == RediSearchQueryType.DELETE) {
      result = searchResult.getDocuments().stream().map(this::parseDocumentResult).toList();
    }

    return processor.processResult(result);
  }

  @Override
  protected Object executeDeleteQuery(Object[] parameters) {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    String baseQuery = prepareQuery(parameters, true);
    AggregationBuilder aggregation = new AggregationBuilder(baseQuery);

    String[] fields = Stream.concat(Stream.of("@__key"), queryOrParts.stream().flatMap(List::stream).filter(pair -> pair
        .getSecond() == QueryClause.IS_NULL || pair.getSecond() == QueryClause.IS_NOT_NULL).map(pair -> String.format(
            "@%s", pair.getFirst()))).toArray(String[]::new);
    aggregation.load(fields);

    for (List<Pair<String, QueryClause>> orPartParts : queryOrParts) {
      for (Pair<String, QueryClause> pair : orPartParts) {
        if (pair.getSecond() == QueryClause.IS_NULL) {
          if (hasIndexMissing(pair.getFirst())) {
            aggregation.filter("ismissing(@" + pair.getFirst() + ")");
          } else {
            aggregation.filter("!exists(@" + pair.getFirst() + ")");
          }
        } else if (pair.getSecond() == QueryClause.IS_NOT_NULL) {
          if (hasIndexMissing(pair.getFirst())) {
            aggregation.filter("!ismissing(@" + pair.getFirst() + ")");
          } else {
            aggregation.filter("exists(@" + pair.getFirst() + ")");
          }
        }
      }
    }

    aggregation.sortBy(aggregationSortedFields.toArray(new SortedField[] {}));
    aggregation.limit(0, redisOMProperties.getRepository().getQuery().getLimit());
    aggregation.dialect(dialect.getValue());

    AggregationResult aggregationResult = ops.aggregate(aggregation);
    List<String> keys = aggregationResult.getResults().stream().map(d -> d.get("__key").toString()).toList();

    Class<?> returnType = queryMethod.getReturnedObjectType();
    if (Number.class.isAssignableFrom(returnType) || returnType.equals(int.class) || returnType.equals(
        long.class) || returnType.equals(short.class)) {
      if (keys.isEmpty()) {
        return 0;
      } else {
        return modulesOperations.template().delete(keys);
      }
    } else {
      if (keys.isEmpty()) {
        return Collections.emptyList();
      } else {
        var entities = modulesOperations.opsForJSON().mget(this.domainType, keys.toArray(new String[0]));
        modulesOperations.template().delete(keys);
        return entities;
      }
    }
  }

  @Override
  protected Object executeNullQuery(Object[] parameters) {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    String baseQuery = prepareQuery(parameters, true);

    AggregationBuilder aggregation = new AggregationBuilder(baseQuery);

    String[] fields = Stream.concat(Stream.of("@__key"), queryOrParts.stream().flatMap(List::stream).filter(pair -> pair
        .getSecond() == QueryClause.IS_NULL || pair.getSecond() == QueryClause.IS_NOT_NULL).map(pair -> String.format(
            "@%s", pair.getFirst()))).toArray(String[]::new);
    aggregation.load(fields);

    for (List<Pair<String, QueryClause>> orPartParts : queryOrParts) {
      for (Pair<String, QueryClause> pair : orPartParts) {
        if (pair.getSecond() == QueryClause.IS_NULL) {
          if (hasIndexMissing(pair.getFirst())) {
            aggregation.filter("ismissing(@" + pair.getFirst() + ")");
          } else {
            aggregation.filter("!exists(@" + pair.getFirst() + ")");
          }
        } else if (pair.getSecond() == QueryClause.IS_NOT_NULL) {
          if (hasIndexMissing(pair.getFirst())) {
            aggregation.filter("!ismissing(@" + pair.getFirst() + ")");
          } else {
            aggregation.filter("exists(@" + pair.getFirst() + ")");
          }
        }
      }
    }

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

    if (needsLimit) {
      if ((limit != null) || (offset != null)) {
        aggregation.limit(offset != null ? offset : 0, limit != null ? limit : 0);
      } else {
        aggregation.limit(0, redisOMProperties.getRepository().getQuery().getLimit());
      }
    }

    aggregation.dialect(dialect.getValue());

    AggregationResult aggregationResult = ops.aggregate(aggregation);
    String[] keys = aggregationResult.getResults().stream().map(d -> d.get("__key").toString()).toArray(String[]::new);

    var entities = modulesOperations.opsForJSON().mget(domainType, keys);

    if (!queryMethod.isCollectionQuery()) {
      return entities.isEmpty() ? null : entities.get(0);
    } else {
      return entities;
    }
  }

  @Override
  protected String prepareQuery(final Object[] parameters, boolean excludeNullParams) {
    logger.debug(String.format("parameters: %s", Arrays.toString(parameters)));
    logger.info(String.format("Preparing query for method: %s, isMapContainsQuery: %s", queryMethod.getName(),
        isMapContainsQuery));
    List<Object> params = new ArrayList<>(Arrays.asList(parameters));
    StringBuilder preparedQuery = new StringBuilder();
    boolean multipleOrParts = queryOrParts.size() > 1;
    logger.debug(String.format("queryOrParts: %s", queryOrParts.size()));
    logger.info(String.format("queryOrParts details: %s", queryOrParts));
    if (!queryOrParts.isEmpty()) {
      preparedQuery.append(queryOrParts.stream().map(qop -> {
        String orPart = multipleOrParts ? "(" : "";
        orPart = orPart + qop.stream().map(fieldClauses -> {
          if (excludeNullParams && (fieldClauses.getSecond() == QueryClause.IS_NULL || fieldClauses
              .getSecond() == QueryClause.IS_NOT_NULL)) {
            return "";
          }
          String fieldName = QueryUtils.escape(fieldClauses.getFirst());
          QueryClause queryClause = fieldClauses.getSecond();
          logger.info(String.format("Processing field: %s with queryClause: %s", fieldName, queryClause));
          int paramsCnt = queryClause.getClauseTemplate().getNumberOfArguments();

          Object[] ps = params.subList(0, paramsCnt).toArray();
          params.subList(0, paramsCnt).clear();

          return queryClause.prepareQuery(fieldName, ps);
        }).collect(Collectors.joining(" "));
        orPart = orPart + (multipleOrParts ? ")" : "");

        return orPart;
      }).collect(Collectors.joining(" | ")));
    } else {
      @SuppressWarnings(
        "unchecked"
      ) Iterator<Parameter> iterator = (Iterator<Parameter>) queryMethod.getParameters().iterator();
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

          if (parameters[index] instanceof java.util.Collection<?> c) {
            v = c.stream().map(Object::toString).collect(Collectors.joining(" | "));
          } else {
            v = parameters[index].toString();
          }

          var regex = "(\\$" + Pattern.quote(key) + "(?![a-zA-Z0-9_]))(\\W+|\\*|\\+|$)?";
          preparedQuery = new StringBuilder(preparedQuery.toString().replaceAll(regex, v + "$2"));
        }
        index++;
      }
    }

    if (preparedQuery.toString().isBlank()) {
      preparedQuery.append("*");
    }

    logger.info(String.format("Final query string: %s", preparedQuery));
    logger.debug(String.format("query: %s", preparedQuery));

    return preparedQuery.toString();
  }

  // ---------------------------------------------------------------------------
  // RSQ-specific helpers
  // ---------------------------------------------------------------------------

  private Object parseDocumentResult(redis.clients.jedis.search.Document doc) {
    if (doc == null) {
      return null;
    }

    Gson gsonInstance = getGson();
    Object entity;

    if (doc.get("$") != null) {
      entity = switch (dialect) {
        case ONE, TWO -> {
          String jsonString = SafeEncoder.encode((byte[]) doc.get("$"));
          yield gsonInstance.fromJson(jsonString, domainType);
        }
        case THREE -> gsonInstance.fromJson(gsonInstance.fromJson(SafeEncoder.encode((byte[]) doc.get("$")),
            JsonArray.class).get(0), domainType);
      };
    } else {
      JsonObject jsonObject = new JsonObject();

      for (Entry<String, Object> entry : doc.getProperties()) {
        String fieldName = entry.getKey();
        Object fieldValue = entry.getValue();

        String valueStr;
        if (fieldValue instanceof byte[]) {
          valueStr = SafeEncoder.encode((byte[]) fieldValue);
        } else {
          valueStr = String.valueOf(fieldValue);
        }

        boolean isPointField = false;
        try {
          Field domainField = ReflectionUtils.findField(domainType, fieldName);
          if (domainField != null && domainField.getType() == Point.class) {
            isPointField = true;
          }
        } catch (Exception e) {
          // Ignore - field might not exist in projection
        }

        if (isPointField && valueStr.contains(",") && !valueStr.startsWith("\"")) {
          jsonObject.addProperty(fieldName, valueStr);
        } else if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
          jsonObject.addProperty(fieldName, valueStr.substring(1, valueStr.length() - 1));
        } else if (valueStr.equals("true") || valueStr.equals("false")) {
          jsonObject.addProperty(fieldName, Boolean.parseBoolean(valueStr));
        } else if (valueStr.equals("1") && fieldName.equals("active")) {
          jsonObject.addProperty(fieldName, true);
        } else if (valueStr.equals("0") && fieldName.equals("active")) {
          jsonObject.addProperty(fieldName, false);
        } else {
          try {
            if (valueStr.contains(".")) {
              jsonObject.addProperty(fieldName, Double.parseDouble(valueStr));
            } else {
              jsonObject.addProperty(fieldName, Long.parseLong(valueStr));
            }
          } catch (NumberFormatException e) {
            jsonObject.addProperty(fieldName, valueStr);
          }
        }
      }

      entity = gsonInstance.fromJson(jsonObject, domainType);
    }

    return ObjectUtils.populateRedisKey(entity, doc.getId());
  }

  private Gson getGson() {
    if (gson == null) {
      gson = gsonBuilder.create();
    }
    return gson;
  }

  private void processMapContainsQuery(String methodName) {
    String queryPart = methodName.replaceFirst("^(find|delete|remove)By", "");
    String[] clauses = queryPart.split("(?=And)|(?=Or)");

    List<Pair<String, QueryClause>> currentOrPart = new ArrayList<>();

    for (String clause : clauses) {
      String cleanClause = clause.replaceFirst("^(And|Or)", "");

      if (cleanClause.contains("MapContains")) {
        Pattern pattern = Pattern.compile(
            "([A-Za-z]+)MapContains([A-Za-z]+)(GreaterThan|LessThan|After|Before|Between|NotEqual|In)?");
        Matcher matcher = pattern.matcher(cleanClause);

        if (matcher.find()) {
          String mapFieldName = matcher.group(1);
          String nestedFieldName = matcher.group(2);
          String operator = matcher.group(3);

          String originalMapFieldName = mapFieldName;
          mapFieldName = Character.toLowerCase(mapFieldName.charAt(0)) + mapFieldName.substring(1);
          nestedFieldName = Character.toLowerCase(nestedFieldName.charAt(0)) + nestedFieldName.substring(1);

          Field mapField = ReflectionUtils.findField(domainType, mapFieldName);
          if (mapField == null) {
            mapField = ReflectionUtils.findField(domainType, originalMapFieldName);
            if (mapField != null) {
              mapFieldName = originalMapFieldName;
            }
          }
          logger.debug(String.format("Looking for Map field '%s' (or '%s') in %s: %s", mapFieldName,
              originalMapFieldName, domainType.getSimpleName(), mapField != null ? "FOUND" : "NOT FOUND"));
          if (mapField != null && Map.class.isAssignableFrom(mapField.getType())) {
            String mapFieldNameForIndex = mapFieldName;
            if (mapField.isAnnotationPresent(Indexed.class)) {
              Indexed mapIndexed = mapField.getAnnotation(Indexed.class);
              if (mapIndexed.alias() != null && !mapIndexed.alias().isEmpty()) {
                mapFieldNameForIndex = mapIndexed.alias();
              }
            }
            Optional<Class<?>> maybeValueType = ObjectUtils.getMapValueClass(mapField);
            if (maybeValueType.isPresent()) {
              Class<?> valueType = maybeValueType.get();
              Field nestedField = ReflectionUtils.findField(valueType, nestedFieldName);
              if (nestedField != null) {
                String actualNestedFieldName = nestedFieldName;
                if (nestedField.isAnnotationPresent(Indexed.class)) {
                  Indexed indexed = nestedField.getAnnotation(Indexed.class);
                  if (indexed.alias() != null && !indexed.alias().isEmpty()) {
                    actualNestedFieldName = indexed.alias();
                  }
                }
                String indexFieldName = mapFieldNameForIndex + "_" + actualNestedFieldName;

                Class<?> nestedFieldType = ClassUtils.resolvePrimitiveIfNecessary(nestedField.getType());
                FieldType redisFieldType = getRedisFieldType(nestedFieldType);

                Part.Type partType = Part.Type.SIMPLE_PROPERTY;
                if ("GreaterThan".equals(operator)) {
                  partType = Part.Type.GREATER_THAN;
                } else if ("LessThan".equals(operator)) {
                  partType = Part.Type.LESS_THAN;
                } else if ("Between".equals(operator)) {
                  partType = Part.Type.BETWEEN;
                } else if ("NotEqual".equals(operator)) {
                  partType = Part.Type.NOT_IN;
                } else if ("In".equals(operator)) {
                  partType = Part.Type.IN;
                }

                if (redisFieldType != null) {
                  QueryClause queryClause = QueryClause.get(redisFieldType, partType);
                  currentOrPart.add(Pair.of(indexFieldName, queryClause));
                  logger.debug(String.format("Added MapContains field: %s with clause: %s", indexFieldName,
                      queryClause));
                }
              }
            }
          }
        }
      } else {
        String fieldName = cleanClause.replaceAll("(GreaterThan|LessThan|Between|NotEqual|In).*", "");
        fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);

        Field field = ReflectionUtils.findField(domainType, fieldName);
        if (field != null) {
          Part.Type partType = Part.Type.SIMPLE_PROPERTY;
          if (clause.contains("GreaterThan")) {
            partType = Part.Type.GREATER_THAN;
          } else if (clause.contains("LessThan")) {
            partType = Part.Type.LESS_THAN;
          }

          String actualFieldName = fieldName;
          if (field.isAnnotationPresent(Indexed.class)) {
            Indexed indexed = field.getAnnotation(Indexed.class);
            if (indexed.alias() != null && !indexed.alias().isEmpty()) {
              actualFieldName = indexed.alias();
            }
          }

          Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());
          FieldType redisFieldType = getRedisFieldType(fieldType);
          if (redisFieldType != null) {
            QueryClause queryClause = QueryClause.get(redisFieldType, partType);
            currentOrPart.add(Pair.of(actualFieldName, queryClause));
          }
        }
      }

      if (clause.startsWith("And") || clause.startsWith("Or")) {
        if (clause.startsWith("Or") && !currentOrPart.isEmpty()) {
          queryOrParts.add(new ArrayList<>(currentOrPart));
          currentOrPart.clear();
        }
      }
    }

    if (!currentOrPart.isEmpty()) {
      queryOrParts.add(currentOrPart);
    }
  }

  /**
   * Checks if a QueryClause represents a lexicographic query.
   */
  private boolean isLexicographicClause(QueryClause clause) {
    return LEXICOGRAPHIC_QUERY_CLAUSES.contains(clause);
  }

  private boolean isLexicographicPartType(Part.Type partType) {
    return LEXICOGRAPHIC_PART_TYPES.contains(partType);
  }

  private QueryClause getLexicographicQueryClause(FieldType fieldType, Part.Type partType) {
    if (fieldType == FieldType.TEXT) {
      return switch (partType) {
        case GREATER_THAN -> QueryClause.TEXT_GREATER_THAN;
        case LESS_THAN -> QueryClause.TEXT_LESS_THAN;
        case GREATER_THAN_EQUAL -> QueryClause.TEXT_GREATER_THAN_EQUAL;
        case LESS_THAN_EQUAL -> QueryClause.TEXT_LESS_THAN_EQUAL;
        case BETWEEN -> QueryClause.TEXT_BETWEEN;
        default -> QueryClause.get(fieldType, partType);
      };
    } else if (fieldType == FieldType.TAG) {
      return switch (partType) {
        case GREATER_THAN -> QueryClause.TAG_GREATER_THAN;
        case LESS_THAN -> QueryClause.TAG_LESS_THAN;
        case GREATER_THAN_EQUAL -> QueryClause.TAG_GREATER_THAN_EQUAL;
        case LESS_THAN_EQUAL -> QueryClause.TAG_LESS_THAN_EQUAL;
        case BETWEEN -> QueryClause.TAG_BETWEEN;
        default -> QueryClause.get(fieldType, partType);
      };
    }
    return QueryClause.get(fieldType, partType);
  }
}
