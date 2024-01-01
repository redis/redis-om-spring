package com.redis.om.spring.search.stream;

import com.google.gson.Gson;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.ReducerFunction;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import redis.clients.jedis.search.aggr.*;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;

import java.time.Duration;
import java.util.*;

public class AggregationStreamImpl<E, T> implements AggregationStream<T> {
  private final Class<E> entityClass;
  private final boolean isDocument;
  private final AggregationBuilder aggregation;
  private Group currentGroup;
  private ReducerFieldPair currentReducer;
  private final MappingRedisOMConverter mappingConverter;
  private final Gson gson;

  private final SearchOperations<String> search;
  private final Set<String> returnFields = new LinkedHashSet<>();
  private final Map<String, Class<?>> returnFieldsTypeHints = new HashMap<>();

  private static final Integer MAX_LIMIT = 10000;
  private boolean limitSet = false;

  private static class ReducerFieldPair {
    private String alias;
    private final Reducer reducer;
    private final MetamodelField<?, ?> field;

    private final ReducerFunction reducerFunction;

    private ReducerFieldPair(Reducer reducer, MetamodelField<?, ?> field, ReducerFunction reducerFunction) {
      this.reducer = reducer;
      this.field = field;
      this.reducerFunction = reducerFunction;
    }

    public static ReducerFieldPair of(Reducer reducer, MetamodelField<?, ?> field, ReducerFunction reducerFunction) {
      return new ReducerFieldPair(reducer, field, reducerFunction);
    }

    public  Reducer getReducer() {
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

  @SafeVarargs
  public AggregationStreamImpl(String searchIndex, RedisModulesOperations<String> modulesOperations, Gson gson, Class<E> entityClass, String query,
      MetamodelField<E, ?>... fields) {
    this.entityClass = entityClass;
    search = modulesOperations.opsForSearch(searchIndex);
    aggregation = new AggregationBuilder(query);
    isDocument = entityClass.isAnnotationPresent(Document.class);
    this.gson = gson;
    this.mappingConverter = new MappingRedisOMConverter(null,
        new ReferenceResolverImpl(modulesOperations.template()));
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
          SortedField sf = new SortedField(o.getProperty(),
            o.getDirection() == Direction.ASC ? SortOrder.ASC : SortOrder.DESC);
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
  public AggregationStream<T> limit(int offset, int limit) {
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

  @SuppressWarnings("unchecked")
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
              mappedValues.add(
                  rawList.stream().map(e -> e != null ? Long.parseLong(e.toString()) : 0L).toList());
            } else if (listContents == Integer.class) {
              mappedValues.add(
                  rawList.stream().map(e -> e != null ? Integer.parseInt(e.toString()) : 0).toList());
            } else if (listContents == Double.class) {
              mappedValues.add(
                  rawList.stream().map(e -> e != null ? Double.parseDouble(e.toString()) : 0).toList());
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
        case 8 -> Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7]);
        case 9 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8]);
        case 10 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9]);
        case 11 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10]);
        case 12 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11]);
        case 13 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12]);
        case 14 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12], values[13]);
        case 15 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12], values[13], values[14]);
        case 16 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15]);
        case 17 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15], values[16]);
        case 18 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15], values[16], values[17]);
        case 19 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15], values[16], values[17], values[18]);
        case 20 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15], values[16], values[17], values[18], values[19]);
        default -> Tuples.of();
      };
    }).toList();
    return (List<R>) asList;
  }

  // Cursor API

  @Override
  public AggregationStream<T> cursor(int count, Duration timeout) {
    applyCurrentGroupBy();
    aggregation.cursor(count, timeout.toMillis());
    return this;
  }

  @Override
  public <R extends T> Slice<R> toList(PageRequest pageRequest, Class<?>... contentTypes) {
    applyCurrentGroupBy();
    aggregation.cursor(pageRequest.getPageSize(), 300000);
    return new AggregationPage(this, pageRequest, entityClass, gson, mappingConverter, isDocument);
  }

  @Override
  public <R extends T> Slice<R> toList(PageRequest pageRequest, Duration timeout, Class<?>... contentTypes) {
    applyCurrentGroupBy();
    aggregation.cursor(pageRequest.getPageSize(), timeout.toMillis());
    return new AggregationPage(this, pageRequest, entityClass, gson, mappingConverter, isDocument);
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
      case SUM, MIN, MAX, QUANTILE, FIRST_VALUE, TOLIST, RANDOM_SAMPLE ->
        fieldTargetClass != null ? fieldTargetClass : String.class;
      case AVG, STDDEV -> Double.class;
    };
  }

  List<E> toEntityList(AggregationResult aggregationResult) {
    if (isDocument) {
      return aggregationResult.getResults().stream()
          .map(d -> gson.fromJson(d.get("$").toString(), entityClass)).toList();
    } else {
      return aggregationResult.getResults().stream().map(h -> (E) ObjectUtils.mapToObject(h, entityClass, mappingConverter)).toList();
    }
  }

}
