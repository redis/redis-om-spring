package com.redis.om.spring.repository.query;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
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
import org.springframework.data.repository.query.parser.PartTree;
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
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteQueryExecutor;
import com.redis.om.spring.repository.query.bloom.BloomQueryExecutor;
import com.redis.om.spring.repository.query.clause.QueryClause;
import com.redis.om.spring.repository.query.countmin.CountMinQueryExecutor;
import com.redis.om.spring.repository.query.cuckoo.CuckooQueryExecutor;
import com.redis.om.spring.repository.query.lexicographic.LexicographicQueryExecutor;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Schema.FieldType;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.*;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;
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
 * <p>The class automatically handles query optimization, including projection support, pagination,
 * sorting, and parameter binding. It integrates with Spring Data's query infrastructure to provide
 * seamless repository method execution.
 *
 * @author Redis OM Spring Team
 * @see RepositoryQuery
 * @see QueryMethod
 * @see RediSearchIndexer
 * @see RedisModulesOperations
 * @since 1.0
 */
public class RediSearchQuery implements RepositoryQuery {

  private static final Log logger = LogFactory.getLog(RediSearchQuery.class);

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
   * This utility method centralizes the logic for mapping Java types to Redis field types.
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
      return null; // Unsupported type
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
      return null; // Unsupported type
    }
  }

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
  private final CountMinQueryExecutor countMinQueryExecutor;
  private final AutoCompleteQueryExecutor autoCompleteQueryExecutor;
  private final LexicographicQueryExecutor lexicographicQueryExecutor;
  private final GsonBuilder gsonBuilder;
  private final RediSearchIndexer indexer;
  private final EntityStream entityStream;
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
  private boolean isMapContainsQuery;
  private Dialect dialect = Dialect.TWO;

  /**
   * Creates a new RediSearchQuery instance for the given repository method.
   *
   * <p>This constructor analyzes the provided query method to determine the query type and strategy.
   * It examines method annotations ({@code @Query}, {@code @Aggregation}, {@code @UseDialect}),
   * method naming patterns, and parameter types to configure the appropriate query execution plan.
   *
   * <p>The constructor performs several key initialization tasks:
   * <ul>
   * <li>Determines query type (search, aggregation, delete, etc.) based on method analysis</li>
   * <li>Parses method name using Spring Data's PartTree for derived queries</li>
   * <li>Configures field mappings and query clauses based on entity annotations</li>
   * <li>Sets up specialized query executors for probabilistic data structures</li>
   * <li>Initializes pagination, sorting, and projection capabilities</li>
   * </ul>
   *
   * @param queryMethod             the repository method metadata containing signature and return type information
   * @param metadata                the repository metadata providing domain type and interface details
   * @param indexer                 the RediSearch indexer for managing search indexes and field mappings
   * @param valueExpressionDelegate Spring Data's delegate for value expression evaluation in query methods
   * @param keyValueOperations      low-level Redis key-value operations template
   * @param rmo                     Redis modules operations providing access to RediSearch, RedisJSON, and
   *                                probabilistic data structures
   * @param queryCreator            the query creator class for building Redis queries (currently unused but reserved
   *                                for future extensibility)
   * @param gsonBuilder             pre-configured Gson builder for JSON serialization/deserialization of Redis
   *                                documents
   * @param redisOMProperties       configuration properties for Redis OM Spring behavior and defaults
   *
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
    logger.info(String.format("Creating %s query method", queryMethod.getName()));

    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.indexer = indexer;
    this.queryMethod = queryMethod;
    this.domainType = this.queryMethod.getEntityInformation().getJavaType();
    this.gsonBuilder = gsonBuilder;
    this.redisOMProperties = redisOMProperties;
    this.entityStream = new EntityStreamImpl(modulesOperations, gsonBuilder, indexer);

    bloomQueryExecutor = new BloomQueryExecutor(this, modulesOperations);
    cuckooQueryExecutor = new CuckooQueryExecutor(this, modulesOperations);
    countMinQueryExecutor = new CountMinQueryExecutor(this, modulesOperations);
    autoCompleteQueryExecutor = new AutoCompleteQueryExecutor(this, modulesOperations);
    lexicographicQueryExecutor = new LexicographicQueryExecutor(this, modulesOperations, indexer);

    Class<?> repoClass = metadata.getRepositoryInterface();
    @SuppressWarnings(
      "rawtypes"
    ) Class[] params = queryMethod.getParameters().stream().map(Parameter::getType).toArray(Class[]::new);
    hasLanguageParameter = Arrays.stream(params).anyMatch(c -> c.isAssignableFrom(SearchLanguage.class));
    isANDQuery = QueryClause.hasContainingAllClause(queryMethod.getName());
    // Only detect nested MapContains patterns (e.g., positionsMapContainsCusip)
    // Simple MapContains (e.g., stringValuesMapContains) should use normal processing
    this.isMapContainsQuery = QueryClause.hasMapContainsNestedClause(queryMethod.getName());

    String methodName = queryMethod.getName();
    if (isANDQuery) {
      methodName = QueryClause.getPostProcessMethodName(methodName);
    }

    // Process simple MapContains patterns (e.g., stringValuesMapContains -> stringValues)
    // for PartTree parsing, but not nested patterns (those are handled by processMapContainsQuery)
    if (QueryClause.hasMapContainsClause(methodName) && !QueryClause.hasMapContainsNestedClause(methodName)) {
      methodName = QueryClause.processMapContainsMethodName(methodName);
    }

    // MapContains queries need special handling due to Spring Data's PartTree limitations
    // Don't transform the method name - instead handle it specially later

    try {
      java.lang.reflect.Method method = repoClass.getMethod(queryMethod.getName(), params);

      // set dialect if @UseDialect is present
      if (method.isAnnotationPresent(UseDialect.class)) {
        UseDialect dialectAnnotation = method.getAnnotation(UseDialect.class);
        this.dialect = dialectAnnotation.dialect();
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
      } else if (this.isMapContainsQuery) {
        // Special handling for MapContains queries
        this.type = queryMethod.getName().matches("(?:remove|delete).*") ?
            RediSearchQueryType.DELETE :
            RediSearchQueryType.QUERY;
        this.returnFields = new String[] {};
        processMapContainsQuery(methodName);
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
      logger.debug(String.format("Could not resolved query method %s(%s): %s", queryMethod.getName(), Arrays.toString(
          params), e.getMessage()));
    }
  }

  private void processMapContainsQuery(String methodName) {
    // Parse MapContains patterns manually without using PartTree
    // Pattern: findBy<Field>MapContains<NestedField>[Operator]...

    // Remove the "findBy" or "deleteBy" prefix
    String queryPart = methodName.replaceFirst("^(find|delete|remove)By", "");

    // Split by "And" or "Or" to get individual clauses
    String[] clauses = queryPart.split("(?=And)|(?=Or)");
    boolean isOr = queryPart.contains("Or");

    List<Pair<String, QueryClause>> currentOrPart = new ArrayList<>();

    for (String clause : clauses) {
      // Remove leading And/Or if present
      String cleanClause = clause.replaceFirst("^(And|Or)", "");

      // Check if this clause contains MapContains pattern
      if (cleanClause.contains("MapContains")) {
        // Extract the Map field and nested field
        Pattern pattern = Pattern.compile(
            "([A-Za-z]+)MapContains([A-Za-z]+)(GreaterThan|LessThan|After|Before|Between|NotEqual|In)?");
        Matcher matcher = pattern.matcher(cleanClause);

        if (matcher.find()) {
          String mapFieldName = matcher.group(1);
          String nestedFieldName = matcher.group(2);
          String operator = matcher.group(3);

          // Store original field name before converting
          String originalMapFieldName = mapFieldName;

          // Convert to lowercase first character for standard Java naming
          mapFieldName = Character.toLowerCase(mapFieldName.charAt(0)) + mapFieldName.substring(1);
          nestedFieldName = Character.toLowerCase(nestedFieldName.charAt(0)) + nestedFieldName.substring(1);

          // Find the Map field - try both lowercase and original casing
          Field mapField = ReflectionUtils.findField(domainType, mapFieldName);
          if (mapField == null) {
            // Try with original casing (e.g., "Positions" instead of "positions")
            mapField = ReflectionUtils.findField(domainType, originalMapFieldName);
            if (mapField != null) {
              // Use the actual field name for building the query
              mapFieldName = originalMapFieldName;
            }
          }
          logger.debug(String.format("Looking for Map field '%s' (or '%s') in %s: %s", mapFieldName,
              originalMapFieldName, domainType.getSimpleName(), mapField != null ? "FOUND" : "NOT FOUND"));
          if (mapField != null && Map.class.isAssignableFrom(mapField.getType())) {
            // Check if the Map field has an @Indexed alias
            String mapFieldNameForIndex = mapFieldName;
            if (mapField.isAnnotationPresent(Indexed.class)) {
              Indexed mapIndexed = mapField.getAnnotation(Indexed.class);
              if (mapIndexed.alias() != null && !mapIndexed.alias().isEmpty()) {
                mapFieldNameForIndex = mapIndexed.alias();
              }
            }
            // Get the Map's value type
            Optional<Class<?>> maybeValueType = ObjectUtils.getMapValueClass(mapField);
            if (maybeValueType.isPresent()) {
              Class<?> valueType = maybeValueType.get();

              // Find the nested field in the value type
              Field nestedField = ReflectionUtils.findField(valueType, nestedFieldName);
              if (nestedField != null) {
                // Build the index field name: mapField_nestedField (respecting alias if present)
                String actualNestedFieldName = nestedFieldName;
                if (nestedField.isAnnotationPresent(Indexed.class)) {
                  Indexed indexed = nestedField.getAnnotation(Indexed.class);
                  if (indexed.alias() != null && !indexed.alias().isEmpty()) {
                    actualNestedFieldName = indexed.alias();
                  }
                }
                String indexFieldName = mapFieldNameForIndex + "_" + actualNestedFieldName;

                // Determine the field type and part type
                Class<?> nestedFieldType = ClassUtils.resolvePrimitiveIfNecessary(nestedField.getType());
                FieldType redisFieldType = getRedisFieldType(nestedFieldType);

                // Determine the Part.Type from the operator
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
        // Handle regular field patterns - delegate to standard parsing
        // This is a simplified version - in production would need full parsing
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

          // Check for @Indexed alias on regular fields
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

      // Handle And/Or logic
      if (clause.startsWith("And") || clause.startsWith("Or")) {
        if (clause.startsWith("Or") && !currentOrPart.isEmpty()) {
          queryOrParts.add(new ArrayList<>(currentOrPart));
          currentOrPart.clear();
        }
      }
    }

    // Add the last part
    if (!currentOrPart.isEmpty()) {
      queryOrParts.add(currentOrPart);
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

    // Handle limiting queries (findTop, findFirst, etc.)
    if (pt.isLimiting()) {
      this.limit = pt.getMaxResults();
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
    } else if (field.isAnnotationPresent(Id.class)) {
      // Handle @Id fields that are auto-indexed (without explicit index annotation)
      // @Id fields are automatically indexed as NUMERIC for Number types, TAG for String/others
      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());
      FieldType redisFieldType = getRedisFieldType(fieldType);

      if (redisFieldType == FieldType.NUMERIC) {
        qf.add(Pair.of(key, QueryClause.get(FieldType.NUMERIC, part.getType())));
      } else if (redisFieldType == FieldType.TAG) {
        qf.add(Pair.of(key, QueryClause.get(FieldType.TAG, part.getType())));
      } else {
        // Fallback to TAG for other types (String, UUID, etc.)
        qf.add(Pair.of(key, QueryClause.get(FieldType.TAG, part.getType())));
      }
    } else if (field.isAnnotationPresent(Indexed.class)) {
      Indexed indexAnnotation = field.getAnnotation(Indexed.class);
      String actualKey = indexAnnotation.alias().isBlank() ? key : indexAnnotation.alias();
      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());
      //
      // Any Character class, Enums or Boolean -> Tag Search Field
      //
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
      }
      //
      // Any Numeric class -> Numeric Search Field
      //
      else if (redisFieldType == FieldType.NUMERIC) {
        qf.add(Pair.of(actualKey, QueryClause.get(FieldType.NUMERIC, part.getType())));
      }
      //
      // Set / List
      //
      else if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)) {
        Optional<Class<?>> maybeCollectionType = ObjectUtils.getCollectionElementClass(field);
        if (maybeCollectionType.isPresent()) {
          Class<?> collectionType = maybeCollectionType.get();

          // Check if this is a nested array field with @Indexed(schemaFieldType = SchemaFieldType.NESTED)
          if (indexAnnotation.schemaFieldType() == SchemaFieldType.NESTED) {
            // For nested arrays, we need to create the proper field path
            String nestedFieldName = path.size() > level + 1 ? path.get(level + 1).getSegment() : "";
            if (!nestedFieldName.isEmpty()) {
              // Create the nested field path: arrayField_nestedField
              String nestedKey = field.getName() + "_" + nestedFieldName;

              logger.debug(String.format("Processing nested array field query: %s -> %s", key, nestedKey));

              // Determine the field type for the nested field
              Field nestedField = ReflectionUtils.findField(collectionType, nestedFieldName);
              if (nestedField != null) {
                // Get the alias matching the indexer logic
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
          } else if (getRedisFieldType(collectionType) == FieldType.TAG) {
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
      // Map fields
      //
      else if (Map.class.isAssignableFrom(fieldType)) {
        // For Map fields, queries on the field actually query the indexed values
        // The indexed field has a "_values" suffix in the search index
        String mapValueKey = key + "_values";

        Optional<Class<?>> maybeValueType = ObjectUtils.getMapValueClass(field);
        if (maybeValueType.isPresent()) {
          Class<?> valueType = maybeValueType.get();
          FieldType valueFieldType = getRedisFieldTypeForMapValue(valueType);

          if (valueFieldType != null) {
            qf.add(Pair.of(mapValueKey, QueryClause.get(valueFieldType, part.getType())));
          }
        }
      }
      //
      // Point
      //
      else if (redisFieldType == FieldType.GEO) {
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
    Optional<String> maybeCountMinFilter = countMinQueryExecutor.getCountMinSketch();

    if (maybeBloomFilter.isPresent()) {
      return bloomQueryExecutor.executeBloomQuery(parameters, maybeBloomFilter.get());
    } else if (maybeCuckooFilter.isPresent()) {
      return cuckooQueryExecutor.executeCuckooQuery(parameters, maybeCuckooFilter.get());
    } else if (maybeCountMinFilter.isPresent()) {
      return countMinQueryExecutor.executeCountMinQuery(parameters, maybeCountMinFilter.get());
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
      // Process as lexicographic query
      logger.debug("Processing as lexicographic query");
      preparedQuery = lexicographicQueryExecutor.processLexicographicQuery(queryOrParts, parameters, domainType);
      logger.debug(String.format("Lexicographic query returned: %s", preparedQuery));
      if (preparedQuery == null) {
        preparedQuery = "*"; // fallback
      }
    } else {
      // Normal query processing
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

    // Set query dialect
    query.dialect(dialect.getValue());

    SearchResult searchResult = ops.search(query);

    // what to return
    Object result = null;

    // Check if this is a SearchStream query
    if (SearchStream.class.isAssignableFrom(queryMethod.getReturnedObjectType())) {
      // For SearchStream, create and configure a stream based on the query
      @SuppressWarnings(
        "unchecked"
      ) SearchStream<?> stream = entityStream.of((Class<Object>) domainType);

      // Build the query string using the existing query builder
      String queryString = prepareQuery(parameters, true);

      // Apply the filter if it's not a wildcard query
      if (!queryString.equals("*") && !queryString.isEmpty()) {
        stream = stream.filter(queryString);
      }

      // Apply limit if configured
      if (limit != null && limit > 0) {
        stream = stream.limit(limit);
      }

      // Return the configured stream
      return stream;
    } else if (processor.getReturnedType().getReturnedType() == boolean.class || processor.getReturnedType()
        .getReturnedType() == Boolean.class) {
      // For exists queries, return true if we have any results, false otherwise
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
    if (doc == null) {
      return null;
    }

    Gson gsonInstance = getGson();
    Object entity;

    if (doc.get("$") != null) {
      // Full document case - normal JSON document retrieval
      entity = switch (dialect) {
        case ONE, TWO -> {
          String jsonString = SafeEncoder.encode((byte[]) doc.get("$"));
          yield gsonInstance.fromJson(jsonString, domainType);
        }
        case THREE -> gsonInstance.fromJson(gsonInstance.fromJson(SafeEncoder.encode((byte[]) doc.get("$")),
            JsonArray.class).get(0), domainType);
      };
    } else {
      // Projection case - individual fields returned from Redis search optimization
      // When projection optimization is enabled, Redis returns individual fields instead of full JSON
      // Use Gson's JsonObject to properly handle JSON serialization including escaping and quoting
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

        // Check if this field is a Point type in the domain class
        boolean isPointField = false;
        try {
          Field domainField = ReflectionUtils.findField(domainType, fieldName);
          if (domainField != null && domainField.getType() == Point.class) {
            isPointField = true;
          }
        } catch (Exception e) {
          // Ignore - field might not exist in projection
        }

        // Handle different types based on the raw value from Redis
        if (isPointField && valueStr.contains(",") && !valueStr.startsWith("\"")) {
          // Point field - stored as "lon,lat" in Redis, needs to be quoted for PointTypeAdapter
          jsonObject.addProperty(fieldName, valueStr);
        } else if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
          // Already quoted string - remove quotes and add as property (Gson will re-quote)
          jsonObject.addProperty(fieldName, valueStr.substring(1, valueStr.length() - 1));
        } else if (valueStr.equals("true") || valueStr.equals("false")) {
          // Boolean
          jsonObject.addProperty(fieldName, Boolean.parseBoolean(valueStr));
        } else if (valueStr.equals("1") && fieldName.equals("active")) {
          // Special case for boolean stored as 1/0
          jsonObject.addProperty(fieldName, true);
        } else if (valueStr.equals("0") && fieldName.equals("active")) {
          jsonObject.addProperty(fieldName, false);
        } else {
          // Try to parse as number, otherwise treat as string
          try {
            if (valueStr.contains(".")) {
              jsonObject.addProperty(fieldName, Double.parseDouble(valueStr));
            } else {
              jsonObject.addProperty(fieldName, Long.parseLong(valueStr));
            }
          } catch (NumberFormatException e) {
            // Not a number - treat as string (Gson will handle proper quoting and escaping)
            jsonObject.addProperty(fieldName, valueStr);
          }
        }
      }

      entity = gsonInstance.fromJson(jsonObject, domainType);
    }

    return ObjectUtils.populateRedisKey(entity, doc.getId());
  }

  private Object executeDeleteQuery(Object[] parameters) {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    String baseQuery = prepareQuery(parameters, true);
    AggregationBuilder aggregation = new AggregationBuilder(baseQuery);

    // Load fields with IS_NULL or IS_NOT_NULL query clauses
    String[] fields = Stream.concat(Stream.of("@__key"), queryOrParts.stream().flatMap(List::stream).filter(pair -> pair
        .getSecond() == QueryClause.IS_NULL || pair.getSecond() == QueryClause.IS_NOT_NULL).map(pair -> String.format(
            "@%s", pair.getFirst()))).toArray(String[]::new);
    aggregation.load(fields);

    // Apply exists or !exists filter for null parameters
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

    // Set query dialect
    aggregation.dialect(dialect.getValue());

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

    // Handle parameters in the base query
    String preparedQuery = prepareQuery(parameters, true);

    // build the aggregation
    AggregationBuilder aggregation = new AggregationBuilder(preparedQuery);

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

    // Set query dialect
    aggregation.dialect(dialect.getValue());

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

          if (parameters[index] instanceof Collection<?> c) {
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
    String[] fields = Stream.concat(Stream.of("@__key"), queryOrParts.stream().flatMap(List::stream).filter(pair -> pair
        .getSecond() == QueryClause.IS_NULL || pair.getSecond() == QueryClause.IS_NOT_NULL).map(pair -> String.format(
            "@%s", pair.getFirst()))).toArray(String[]::new);

    aggregation.load(fields);

    // Apply exists or !exists filter for null parameters
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

    // Set query dialect
    aggregation.dialect(dialect.getValue());

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

  /**
   * Checks if a field has indexMissing enabled by examining its annotations.
   * 
   * @param fieldName the name of the field to check
   * @return true if the field has indexMissing = true, false otherwise
   */
  private boolean hasIndexMissing(String fieldName) {
    try {
      Field field = ReflectionUtils.findField(domainType, fieldName);
      if (field == null) {
        return false;
      }

      // Check @Indexed annotation
      if (field.isAnnotationPresent(com.redis.om.spring.annotations.Indexed.class)) {
        com.redis.om.spring.annotations.Indexed indexed = field.getAnnotation(
            com.redis.om.spring.annotations.Indexed.class);
        return indexed.indexMissing();
      }

      // Check @Searchable annotation  
      if (field.isAnnotationPresent(com.redis.om.spring.annotations.Searchable.class)) {
        com.redis.om.spring.annotations.Searchable searchable = field.getAnnotation(
            com.redis.om.spring.annotations.Searchable.class);
        return searchable.indexMissing();
      }

      return false;
    } catch (Exception e) {
      logger.debug("Failed to check indexMissing for field: " + fieldName, e);
      return false;
    }
  }

  /**
   * Checks if a Part.Type represents a lexicographic query operation.
   *
   * @param type the query part type to check
   * @return true if the type is GREATER_THAN, LESS_THAN, GREATER_THAN_EQUAL, LESS_THAN_EQUAL, or BETWEEN
   */
  private boolean isLexicographicQueryType(Part.Type type) {
    return LEXICOGRAPHIC_PART_TYPES.contains(type);
  }

  /**
   * Checks if a QueryClause represents a lexicographic query.
   *
   * @param clause the query clause to check
   * @return true if the clause is a lexicographic query clause
   */
  private boolean isLexicographicClause(QueryClause clause) {
    return LEXICOGRAPHIC_QUERY_CLAUSES.contains(clause);
  }

  private boolean isLexicographicPartType(Part.Type partType) {
    return LEXICOGRAPHIC_PART_TYPES.contains(partType);
  }

  private QueryClause getLexicographicQueryClause(FieldType fieldType, Part.Type partType) {
    if (fieldType == FieldType.TEXT) {
      switch (partType) {
        case GREATER_THAN:
          return QueryClause.TEXT_GREATER_THAN;
        case LESS_THAN:
          return QueryClause.TEXT_LESS_THAN;
        case GREATER_THAN_EQUAL:
          return QueryClause.TEXT_GREATER_THAN_EQUAL;
        case LESS_THAN_EQUAL:
          return QueryClause.TEXT_LESS_THAN_EQUAL;
        case BETWEEN:
          return QueryClause.TEXT_BETWEEN;
        default:
          return QueryClause.get(fieldType, partType);
      }
    } else if (fieldType == FieldType.TAG) {
      switch (partType) {
        case GREATER_THAN:
          return QueryClause.TAG_GREATER_THAN;
        case LESS_THAN:
          return QueryClause.TAG_LESS_THAN;
        case GREATER_THAN_EQUAL:
          return QueryClause.TAG_GREATER_THAN_EQUAL;
        case LESS_THAN_EQUAL:
          return QueryClause.TAG_LESS_THAN_EQUAL;
        case BETWEEN:
          return QueryClause.TAG_BETWEEN;
        default:
          return QueryClause.get(fieldType, partType);
      }
    }
    return QueryClause.get(fieldType, partType);
  }
}
