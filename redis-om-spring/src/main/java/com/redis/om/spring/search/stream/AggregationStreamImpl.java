package com.redis.om.spring.search.stream;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;

import com.google.gson.Gson;
import com.redis.om.spring.annotations.Dialect;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.ReducerFunction;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.search.stream.aggregations.filters.AggregationFilter;
import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.aggr.*;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;

/**
 * Default implementation of {@link AggregationStream} that provides Redis aggregation capabilities.
 * This class builds and executes Redis aggregation queries using the RediSearch module,
 * supporting complex data analysis operations like grouping, filtering, sorting, and reducing.
 * 
 * <p>AggregationStreamImpl manages the construction of Redis aggregation pipelines and
 * handles the conversion of results back to Java objects. It supports both document-based
 * and hash-based Redis OM entities.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 * <li>Fluent API for building complex aggregation queries</li>
 * <li>Automatic field loading and type conversion</li>
 * <li>Support for various reducer functions and operations</li>
 * <li>Pagination and cursor-based processing</li>
 * <li>Integration with Spring Data abstractions</li>
 * </ul>
 * 
 * @param <E> the source entity type for the aggregation
 * @param <T> the target type for aggregation results
 * 
 * @since 1.0
 * @see AggregationStream
 */
public class AggregationStreamImpl<E, T> implements AggregationStream<T> {
  private static final Integer MAX_LIMIT = 10000;
  private final Class<E> entityClass;
  private final boolean isDocument;
  private final AggregationBuilder aggregation;
  private final MappingRedisOMConverter mappingConverter;
  private final Gson gson;
  private final SearchOperations<String> search;
  private final Set<String> returnFields = new LinkedHashSet<>();
  private final Map<String, Class<?>> returnFieldsTypeHints = new HashMap<>();
  private Group currentGroup;
  private ReducerFieldPair currentReducer;
  private boolean limitSet = false;
  private final String query;

  /**
   * Constructs a new AggregationStreamImpl for the specified entity type and search criteria.
   * 
   * @param searchIndex       the Redis search index to query against
   * @param modulesOperations the Redis modules operations for executing commands
   * @param gson              the JSON serializer for entity conversion
   * @param entityClass       the class of the source entity type
   * @param query             the base query string for the aggregation
   * @param fields            optional metamodel fields to initially load
   */
  @SafeVarargs
  public AggregationStreamImpl(String searchIndex, RedisModulesOperations<String> modulesOperations, Gson gson,
      Class<E> entityClass, String query, MetamodelField<E, ?>... fields) {
    this.entityClass = entityClass;
    search = modulesOperations.opsForSearch(searchIndex);
    aggregation = new AggregationBuilder(query);
    aggregation.dialect(Dialect.TWO.getValue());
    isDocument = entityClass.isAnnotationPresent(Document.class);
    this.query = query;
    this.gson = gson;
    this.mappingConverter = new MappingRedisOMConverter(null, new ReferenceResolverImpl(modulesOperations.template()));
    createAggregationGroup(fields);
  }

  @Override
  public AggregationStream<T> load(MetamodelField<?, ?>... fields) {
    applyCurrentGroupBy();
    if (fields.length > 0) {
      var aliases = new ArrayList<String>();
      for (MetamodelField<?, ?> mmf : fields) {
        aliases.add(mmf.getSearchAlias());
        returnFieldsTypeHints.put(mmf.getSearchAlias(), mmf.getTargetClass());
      }
      aggregation.load(aliases.stream().map(alias -> String.format("@%s", alias)).toArray(String[]::new));
      returnFields.addAll(aliases);
    }
    return this;
  }

  @Override
  public AggregationStream<T> loadAll() {
    applyCurrentGroupBy();
    aggregation.loadAll();
    return this;
  }

  @Override
  public AggregationStream<T> groupBy(MetamodelField<?, ?>... fields) {
    applyCurrentGroupBy();
    createAggregationGroup(true, fields);
    return this;
  }

  @Override
  public AggregationStream<T> reduce(ReducerFunction reducer, MetamodelField<?, ?> field, Object... params) {

    String alias = null;
    if (field != null) {
      alias = field.getSearchAlias();
      returnFields.remove(alias.startsWith("@") ? alias.substring(1) : alias);
    }

    if (currentGroup == null) {
      createAggregationGroup(true);
    }

    applyCurrentReducer();
    Reducer r = null;

    switch (reducer) {
      case COUNT -> r = Reducers.count();
      case COUNT_DISTINCT -> r = Reducers.count_distinct(alias);
      case COUNT_DISTINCTISH -> r = Reducers.count_distinctish(alias);
      case SUM -> r = Reducers.sum(alias);
      case MIN -> r = Reducers.min(alias);
      case MAX -> r = Reducers.max(alias);
      case AVG -> r = Reducers.avg(alias);
      case STDDEV -> r = Reducers.stddev(alias);
      case QUANTILE -> {
        double percentile = Double.parseDouble(params[0].toString());
        r = Reducers.quantile(alias, percentile);
      }
      case TOLIST -> r = Reducers.to_list(alias);
      case FIRST_VALUE -> {
        if (params.length > 0 && params[0].getClass().isAssignableFrom(Order.class)) {
          Order o = (Order) params[0];
          SortedField sf = new SortedField(o.getProperty(), o.getDirection() == Direction.ASC ?
              SortOrder.ASC :
              SortOrder.DESC);
          r = Reducers.first_value(alias, sf);
        } else {
          r = Reducers.first_value(alias);
        }
      }
      case RANDOM_SAMPLE -> {
        int sampleSize = Integer.parseInt(params[0].toString());
        r = Reducers.random_sample(alias, sampleSize);
      }
    }
    if (r != null) {
      currentReducer = ReducerFieldPair.of(r, field, reducer);
    }

    return this;
  }

  @Override
  public AggregationStream<T> reduce(ReducerFunction reducer) {
    return reduce(reducer, (MetamodelField<?, ?>) null);
  }

  @Override
  public AggregationStream<T> reduce(ReducerFunction reducer, String alias, Object... params) {
    return reduce(reducer, new MetamodelField<>(alias, String.class, true), params);
  }

  private boolean applyCurrentReducer() {
    if (currentReducer != null) {
      Reducer cr = currentReducer.getReducer();
      MetamodelField<?, ?> crField = currentReducer.getField();
      if (currentReducer.getAlias() == null) {
        currentReducer.setAlias(currentReducer.getReducerFunction().name().toLowerCase());
      }
      currentGroup.reduce(cr);
      returnFields.add(currentReducer.getAlias());
      returnFieldsTypeHints.put(currentReducer.getAlias(), getTypeHintForReducer(currentReducer, crField));
      currentReducer = null;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public AggregationStream<T> apply(String expression, String alias) {
    applyCurrentGroupBy();
    aggregation.apply(expression, alias);
    returnFields.add(alias);
    return this;
  }

  @Override
  public AggregationStream<T> as(String alias) {
    if (currentReducer != null) {
      currentReducer.setAlias(alias);
    }
    return this;
  }

  @Override
  public AggregationStream<T> sorted(Order... fields) {
    applyCurrentGroupBy();
    aggregation.sortBy(mapToSortedFields(fields));
    returnFields.addAll(extractAliases(fields));
    return this;
  }

  @Override
  public AggregationStream<T> sorted(int max, Order... fields) {
    applyCurrentGroupBy();
    aggregation.sortBy(max, mapToSortedFields(fields));
    returnFields.addAll(extractAliases(fields));
    return this;
  }

  private List<String> extractAliases(Order[] fields) {
    return Arrays.stream(fields) //
        .map(f -> f.getProperty().startsWith("@") ? f.getProperty().substring(1) : f.getProperty()) //
        .toList();
  }

  private SortedField[] mapToSortedFields(Order... fields) {
    return Arrays.stream(fields) //
        .map(f -> f.isDescending() ? SortedField.desc(f.getProperty()) : SortedField.asc(f.getProperty())).toList() //
        .toArray(SortedField[]::new);
  }

  @Override
  public AggregationStream<T> limit(int limit) {
    applyCurrentGroupBy();
    aggregation.limit(limit);
    limitSet = true;
    return this;
  }

  @Override
  public AggregationStream<T> limit(int limit, int offset) {
    applyCurrentGroupBy();
    aggregation.limit(offset, limit);
    limitSet = true;
    return this;
  }

  @Override
  public AggregationStream<T> filter(String... filters) {
    applyCurrentGroupBy();
    for (String filter : filters) {
      aggregation.filter(filter);
    }
    return this;
  }

  public AggregationStream<T> filter(AggregationFilter... filters) {
    applyCurrentGroupBy();
    for (AggregationFilter filter : filters) {
      this.aggregation.filter(filter.getFilter());
      this.returnFields.remove(filter.getField());
    }

    return this;
  }

  @Override
  public AggregationResult aggregate() {
    applyCurrentGroupBy();
    return search.aggregate(aggregation);
  }

  @Override
  public AggregationResult aggregateVerbatim() {
    aggregation.verbatim();
    return search.aggregate(aggregation);
  }

  @Override
  public AggregationResult aggregate(Duration timeout) {
    if (!limitSet) {
      aggregation.limit(MAX_LIMIT);
    }
    aggregation.timeout(timeout.toMillis());
    return search.aggregate(aggregation);
  }

  @Override
  public AggregationResult aggregateVerbatim(Duration timeout) {
    if (!limitSet) {
      aggregation.limit(MAX_LIMIT);
    }
    aggregation.timeout(timeout.toMillis());
    aggregation.verbatim();
    return search.aggregate(aggregation);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public <R extends T> List<R> toList(Class<?>... contentTypes) {
    applyCurrentGroupBy();

    if (!limitSet) {
      aggregation.limit(MAX_LIMIT);
    }

    // execute the aggregation
    AggregationResult aggregationResult = search.aggregate(aggregation);

    // is toList called with the same type as the stream?
    if (contentTypes.length == 1 && contentTypes[0].isAssignableFrom(entityClass)) {
      return (List<R>) toEntityList(aggregationResult);
    }

    // package the results
    String[] labels = returnFields.toArray(String[]::new);

    List<?> asList = aggregationResult.getResults().stream().map(m -> { //
      List<Object> mappedValues = new ArrayList<>();
      for (int i = 0; i < labels.length; i++) {
        Object raw = m.get(labels[i]);
        if (contentTypes[i] == String.class) {
          mappedValues.add(raw != null ? raw : "");
        } else if (contentTypes[i] == Long.class) {
          mappedValues.add(raw != null ? Long.parseLong(raw.toString()) : 0L);
        } else if (contentTypes[i] == Integer.class) {
          mappedValues.add(raw != null ? Integer.parseInt(raw.toString()) : 0);
        } else if (contentTypes[i] == Double.class) {
          mappedValues.add(raw != null ? Double.parseDouble(raw.toString()) : 0);
        } else if (contentTypes[i] == List.class && List.class.isAssignableFrom(raw.getClass())) {
          Class<?> listContents = returnFieldsTypeHints.get(labels[i]);
          List<?> rawList = (List<?>) raw;
          if (listContents != null) {
            if (listContents == String.class) {
              mappedValues.add(rawList.stream().map(e -> e != null ? e : "").toList());
            } else if (listContents == Long.class) {
              mappedValues.add(rawList.stream().map(e -> e != null ? Long.parseLong(e.toString()) : 0L).toList());
            } else if (listContents == Integer.class) {
              mappedValues.add(rawList.stream().map(e -> e != null ? Integer.parseInt(e.toString()) : 0).toList());
            } else if (listContents == Double.class) {
              mappedValues.add(rawList.stream().map(e -> e != null ? Double.parseDouble(e.toString()) : 0).toList());
            } else {
              mappedValues.add(rawList);
            }
          } else {
            mappedValues.add(rawList);
          }
        }
      }

      Object[] values = mappedValues.toArray();

      return switch (labels.length) {
        case 1 -> Tuples.of(labels, values[0]);
        case 2 -> Tuples.of(labels, values[0], values[1]);
        case 3 -> Tuples.of(labels, values[0], values[1], values[2]);
        case 4 -> Tuples.of(labels, values[0], values[1], values[2], values[3]);
        case 5 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4]);
        case 6 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5]);
        case 7 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6]);
        case 8 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7]);
        case 9 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8]);
        case 10 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9]);
        case 11 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10]);
        case 12 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10], values[11]);
        case 13 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10], values[11], values[12]);
        case 14 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10], values[11], values[12], values[13]);
        case 15 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14]);
        case 16 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15]);
        case 17 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15],
            values[16]);
        case 18 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15],
            values[16], values[17]);
        case 19 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15],
            values[16], values[17], values[18]);
        case 20 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15],
            values[16], values[17], values[18], values[19]);
        default -> Tuples.of();
      };
    }).toList();
    return (List<R>) asList;
  }

  @Override
  public String backingQuery() {
    return query;
  }

  @Override
  public AggregationStream<T> cursor(int count, Duration timeout) {
    applyCurrentGroupBy();
    aggregation.cursor(count, timeout.toMillis());
    return this;
  }

  // Cursor API

  @Override
  @SuppressWarnings(
    { "unchecked", "rawtypes" }
  )
  public <R extends T> Page<R> toList(Pageable pageRequest, Class<?>... contentTypes) {
    applyCurrentGroupBy();
    aggregation.cursor(pageRequest.getPageSize(), 300000);
    return new AggregationPage(this, pageRequest, entityClass, gson, mappingConverter, isDocument, this.search);
  }

  @Override
  @SuppressWarnings(
    { "unchecked", "rawtypes" }
  )
  public <R extends T> Page<R> toList(Pageable pageRequest, Duration timeout, Class<?>... contentTypes) {
    applyCurrentGroupBy();
    aggregation.cursor(pageRequest.getPageSize(), timeout.toMillis());
    return new AggregationPage(this, pageRequest, entityClass, gson, mappingConverter, isDocument, this.search);
  }

  private void applyCurrentGroupBy() {
    if (currentGroup != null) {
      if (applyCurrentReducer()) {
        aggregation.groupBy(currentGroup);
      }
      currentGroup = null;
    }
  }

  private void createAggregationGroup(MetamodelField<?, ?>... fields) {
    createAggregationGroup(false, fields);
  }

  private void createAggregationGroup(boolean createIfEmpty, MetamodelField<?, ?>... fields) {
    if (fields.length > 0) {
      var aliases = new ArrayList<String>();
      for (MetamodelField<?, ?> mmf : fields) {
        aliases.add(mmf.getSearchAlias());
        returnFieldsTypeHints.put(mmf.getSearchAlias(), mmf.getTargetClass());
      }
      currentGroup = new Group(aliases.stream().map(alias -> String.format("@%s", alias)).toArray(String[]::new));
      returnFields.addAll(aliases);
    } else if (createIfEmpty) {
      currentGroup = new Group();
    }
  }

  private Class<?> getTypeHintForReducer(ReducerFieldPair reducerFieldPair, MetamodelField<?, ?> field) {
    Class<?> fieldTargetClass = field != null ? field.getTargetClass() : null;
    return switch (reducerFieldPair.getReducerFunction()) {
      case COUNT, COUNT_DISTINCT, COUNT_DISTINCTISH -> Long.class;
      case SUM, MIN, MAX, QUANTILE, FIRST_VALUE, TOLIST, RANDOM_SAMPLE -> fieldTargetClass != null ?
          fieldTargetClass :
          String.class;
      case AVG, STDDEV -> Double.class;
    };
  }

  @SuppressWarnings(
    "unchecked"
  )
  List<E> toEntityList(AggregationResult aggregationResult) {
    if (isDocument) {
      return aggregationResult.getResults().stream().map(d -> gson.fromJson(d.get("$").toString(), entityClass))
          .toList();
    } else {
      return aggregationResult.getResults().stream().map(h -> (E) ObjectUtils.mapToObject(h, entityClass,
          mappingConverter)).toList();
    }
  }

  @Override
  public <P> List<P> toProjection(Class<P> projectionClass) {
    applyCurrentGroupBy();

    if (!limitSet) {
      aggregation.limit(MAX_LIMIT);
    }

    // Execute the aggregation
    AggregationResult aggregationResult = search.aggregate(aggregation);

    // Convert results to projections
    return aggregationResult.getResults().stream().map(result -> createProjection(projectionClass, result)).toList();
  }

  @Override
  public List<Map<String, Object>> toMaps() {
    return toMaps(true);
  }

  @Override
  public List<Map<String, Object>> toMaps(boolean includeId) {
    applyCurrentGroupBy();

    if (!limitSet) {
      aggregation.limit(MAX_LIMIT);
    }

    // Execute the aggregation
    AggregationResult aggregationResult = search.aggregate(aggregation);

    // Convert results to maps
    return aggregationResult.getResults().stream().map(result -> {
      Map<String, Object> map = new LinkedHashMap<>();

      // Add ID if requested and available
      if (includeId) {
        // Try common ID field names
        if (result.containsKey("id")) {
          map.put("id", result.get("id"));
        } else if (result.containsKey("_id")) {
          map.put("id", result.get("_id"));
        } else if (result.containsKey("@id")) {
          map.put("id", result.get("@id"));
        }
      }

      // Add all requested fields or all available fields if no specific fields were requested
      if (!returnFields.isEmpty()) {
        for (String field : returnFields) {
          String redisField = field.startsWith("@") ? field : "@" + field;
          String simpleField = field.startsWith("@") ? field.substring(1) : field;

          if (result.containsKey(redisField)) {
            map.put(simpleField, result.get(redisField));
          } else if (result.containsKey(field)) {
            map.put(simpleField, result.get(field));
          }
        }
      } else {
        // If no specific fields were requested, include all non-ID fields from the result
        for (Map.Entry<String, Object> entry : result.entrySet()) {
          String key = entry.getKey();
          String simpleField = key.startsWith("@") ? key.substring(1) : key;

          // Skip ID fields if includeId is false or if they were already processed
          if (!includeId && (key.equals("id") || key.equals("_id") || key.equals("@id"))) {
            continue;
          }

          map.put(simpleField, entry.getValue());
        }
      }

      return map;
    }).toList();
  }

  @SuppressWarnings(
    "unchecked"
  )
  private <P> P createProjection(Class<P> projectionClass, Map<String, Object> data) {
    if (!projectionClass.isInterface()) {
      throw new IllegalArgumentException("Projection class must be an interface");
    }

    InvocationHandler handler = new ProjectionInvocationHandler(data, projectionClass);

    return (P) Proxy.newProxyInstance(projectionClass.getClassLoader(), new Class<?>[] { projectionClass }, handler);
  }

  /**
   * InvocationHandler for dynamic projection proxies.
   */
  private class ProjectionInvocationHandler implements InvocationHandler {
    private final Map<String, Object> data;
    private final Class<?> projectionClass;

    public ProjectionInvocationHandler(Map<String, Object> data, Class<?> projectionClass) {
      this.data = data;
      this.projectionClass = projectionClass;
    }

    // Package-private getter for testing and comparison
    Map<String, Object> getData() {
      return this.data;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      // Handle Object methods
      if (method.getDeclaringClass() == Object.class) {
        switch (method.getName()) {
          case "toString":
            return projectionClass.getSimpleName() + data;
          case "hashCode":
            return data.hashCode();
          case "equals":
            if (args[0] == null)
              return false;
            if (args[0] == proxy)
              return true;
            if (!projectionClass.isInstance(args[0]))
              return false;
            // Compare the underlying data
            try {
              InvocationHandler otherHandler = Proxy.getInvocationHandler(args[0]);
              // Check if it's the same type of handler by checking the class
              if (otherHandler.getClass() == this.getClass()) {
                // Since it's the same class and in the same enclosing instance,
                // we can safely cast and access the data
                ProjectionInvocationHandler otherProjectionHandler = (ProjectionInvocationHandler) otherHandler;
                return data.equals(otherProjectionHandler.getData());
              }
            } catch (Exception e) {
              return false;
            }
            return false;
          default:
            return method.invoke(this, args);
        }
      }

      // Handle getter methods
      String methodName = method.getName();
      if (methodName.startsWith("get") && methodName.length() > 3 && method.getParameterCount() == 0) {
        String propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);

        // Check various field name formats
        Object value = null;
        if (data.containsKey(propertyName)) {
          value = data.get(propertyName);
        } else if (data.containsKey("@" + propertyName)) {
          value = data.get("@" + propertyName);
        } else {
          // Try to find a matching field ignoring case
          for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            String normalizedKey = key.startsWith("@") ? key.substring(1) : key;
            if (normalizedKey.equalsIgnoreCase(propertyName)) {
              value = entry.getValue();
              break;
            }
          }
        }

        // Type conversion if needed
        if (value != null) {
          Class<?> returnType = method.getReturnType();
          return convertValue(value, returnType);
        }

        return null;
      }

      // For non-getter methods, throw exception
      throw new UnsupportedOperationException("Method " + method.getName() + " is not supported on projections");
    }

    private Object convertValue(Object value, Class<?> targetType) {
      if (value == null || targetType.isAssignableFrom(value.getClass())) {
        return value;
      }

      String stringValue = value.toString();

      // Handle common type conversions
      if (targetType == String.class) {
        return stringValue;
      } else if (targetType == Long.class || targetType == long.class) {
        return Long.parseLong(stringValue);
      } else if (targetType == Integer.class || targetType == int.class) {
        return Integer.parseInt(stringValue);
      } else if (targetType == Double.class || targetType == double.class) {
        return Double.parseDouble(stringValue);
      } else if (targetType == Float.class || targetType == float.class) {
        return Float.parseFloat(stringValue);
      } else if (targetType == Boolean.class || targetType == boolean.class) {
        return Boolean.parseBoolean(stringValue);
      }

      // For complex types, try to use gson if it's a JSON string
      if (isDocument && value instanceof String) {
        try {
          return gson.fromJson((String) value, targetType);
        } catch (Exception e) {
          // Fall back to returning the original value
        }
      }

      return value;
    }
  }

  private static class ReducerFieldPair {
    private final Reducer reducer;
    private final MetamodelField<?, ?> field;
    private final ReducerFunction reducerFunction;
    private String alias;

    private ReducerFieldPair(Reducer reducer, MetamodelField<?, ?> field, ReducerFunction reducerFunction) {
      this.reducer = reducer;
      this.field = field;
      this.reducerFunction = reducerFunction;
    }

    public static ReducerFieldPair of(Reducer reducer, MetamodelField<?, ?> field, ReducerFunction reducerFunction) {
      return new ReducerFieldPair(reducer, field, reducerFunction);
    }

    public Reducer getReducer() {
      return this.reducer;
    }

    public MetamodelField<?, ?> getField() {
      return this.field;
    }

    public String getAlias() {
      return this.alias;
    }

    public void setAlias(String alias) {
      this.alias = alias;
      reducer.as(alias);
    }

    public ReducerFunction getReducerFunction() {
      return reducerFunction;
    }

    public boolean equals(final Object o) {
      if (o == this)
        return true;
      if (!(o instanceof ReducerFieldPair other))
        return false;
      if (!other.canEqual(this))
        return false;
      final Object this$reducer = this.getReducer();
      final Object other$reducer = other.getReducer();
      if (!Objects.equals(this$reducer, other$reducer))
        return false;
      final Object this$field = this.getField();
      final Object other$field = other.getField();
      if (!Objects.equals(this$field, other$field))
        return false;
      final Object this$alias = this.getAlias();
      final Object other$alias = other.getAlias();
      return Objects.equals(this$alias, other$alias);
    }

    protected boolean canEqual(final Object other) {
      return other instanceof AggregationStreamImpl.ReducerFieldPair;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $reducer = this.getReducer();
      result = result * PRIME + ($reducer == null ? 43 : $reducer.hashCode());
      final Object $field = this.getField();
      result = result * PRIME + ($field == null ? 43 : $field.hashCode());
      return result;
    }

    public String toString() {
      return "AggregationStreamImpl.ReducerFieldPair(reducer=" + this.getReducer() + ", field=" + this.getField() + ")";
    }
  }

}
