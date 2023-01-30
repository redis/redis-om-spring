package com.redis.om.spring.search.stream;

import com.redis.om.spring.annotations.ReducerFunction;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.tuple.Tuples;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.domain.Sort.Order;
import redis.clients.jedis.search.aggr.*;

import java.time.Duration;
import java.util.*;

public class AggregationStreamImpl<E, T> implements AggregationStream<T> {
  private final AggregationBuilder aggregation;
  private Group currentGroup;
  private ReducerFieldPair currentReducer;

  private final SearchOperations<String> search;
  private final Set<String> returnFields = new LinkedHashSet<>();
  private final Map<String, Class<?>> returnFieldsTypeHints = new HashMap<>();

  @Data @AllArgsConstructor(staticName = "of") private static class ReducerFieldPair {
    @NonNull private Reducer reducer;
    private MetamodelField<?, ?> field;
  }

  @SafeVarargs public AggregationStreamImpl(String searchIndex, RedisModulesOperations<String> modulesOperations, String query,
      MetamodelField<E, ?>... fields) {
    search = modulesOperations.opsForSearch(searchIndex);
    aggregation = new AggregationBuilder(query);
    createAggregationGroup(fields);
  }

  @Override public AggregationStream<T> load(MetamodelField<?, ?>... fields) {
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

  @Override public AggregationStream<T> groupBy(MetamodelField<?, ?>... fields) {
    applyCurrentGroupBy();
    createAggregationGroup(fields);
    return this;
  }

  @Override public AggregationStream<T> reduce(ReducerFunction reducer, MetamodelField<?, ?> field, String... params) {
    if (currentGroup != null) {
      applyCurrentReducer();
      Reducer r = null;
      switch (reducer) {
        case COUNT -> r = Reducers.count();
        case COUNT_DISTINCT -> r = Reducers.count_distinct(field.getSearchAlias());
        case COUNT_DISTINCTISH -> r = Reducers.count_distinctish(field.getSearchAlias());
        case SUM -> r = Reducers.sum(field.getSearchAlias());
        case MIN -> r = Reducers.min(field.getSearchAlias());
        case MAX -> r = Reducers.max(field.getSearchAlias());
        case AVG -> r = Reducers.avg(field.getSearchAlias());
        case STDDEV -> r = Reducers.stddev(field.getSearchAlias());
        case QUANTILE -> {
          double percentile = Double.parseDouble(params[0]);
          r = Reducers.quantile(field.getSearchAlias(), percentile);
        }
        case TOLIST -> r = Reducers.to_list(field.getSearchAlias());
        case FIRST_VALUE -> r = Reducers.first_value(field.getSearchAlias());
        case RANDOM_SAMPLE -> {
          int sampleSize = Integer.parseInt(params[0]);
          r = Reducers.random_sample(field.getSearchAlias(), sampleSize);
        }
      }
      if (r != null) {
        currentReducer = ReducerFieldPair.of(r, field);
      }

    }
    return this;
  }

  private void applyCurrentReducer() {
    if (currentReducer != null) {
      Reducer cr = currentReducer.getReducer();
      MetamodelField<?, ?> crField = currentReducer.getField();
      if (cr.getAlias() == null) {
        cr.setAlias(cr.getName().toLowerCase());
      }
      currentGroup.reduce(cr);
      returnFields.add(cr.getAlias());
      returnFieldsTypeHints.put(cr.getAlias(), getTypeHintForReducer(cr, crField));
    }
  }

  @Override public AggregationStream<T> reduce(ReducerFunction reducer) {
    return reduce(reducer, null);
  }

  @Override public AggregationStream<T> apply(String expression, String alias) {
    applyCurrentGroupBy();
    aggregation.apply(expression, alias);
    returnFields.add(alias);
    return this;
  }

  @Override public AggregationStream<T> as(String alias) {
    if (currentReducer != null) {
      currentReducer.getReducer().setAlias(alias);
    }
    return this;
  }

  @Override public AggregationStream<T> sorted(Order... fields) {
    applyCurrentGroupBy();
    aggregation.sortBy(mapToSortedFields(fields));
    returnFields.addAll(extractAliases(fields));
    return this;
  }

  @Override public AggregationStream<T> sorted(int max, Order... fields) {
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

  @Override public AggregationStream<T> limit(int limit) {
    applyCurrentGroupBy();
    aggregation.limit(limit);
    return this;
  }

  @Override public AggregationStream<T> limit(int offset, int limit) {
    applyCurrentGroupBy();
    aggregation.limit(offset, limit);
    return this;
  }

  @Override public AggregationStream<T> filter(String... filters) {
    applyCurrentGroupBy();
    for (String filter : filters) {
      aggregation.filter(filter);
    }
    return this;
  }

  @Override public AggregationResult aggregate() {
    return search.aggregate(aggregation);
  }

  @Override public AggregationResult aggregateVerbatim() {
    aggregation.verbatim();
    return search.aggregate(aggregation);
  }

  @Override public AggregationResult aggregate(Duration timeout) {
    aggregation.timeout(timeout.toMillis());
    return search.aggregate(aggregation);
  }

  @Override public AggregationResult aggregateVerbatim(Duration timeout) {
    aggregation.timeout(timeout.toMillis());
    aggregation.verbatim();
    return search.aggregate(aggregation);
  }

  @SuppressWarnings("unchecked")
  @Override public <R extends T> List<R> toList(Class<?>... contentTypes) {
    // execute the aggregation
    AggregationResult aggregationResult = search.aggregate(aggregation);

    // package the results
    String[] labels = returnFields.toArray(String[]::new);

    List<?> asList = aggregationResult.getResults().stream().map(m -> { //
      List<Object> mappedValues = new ArrayList<>();
      for (int i = 0; i < labels.length; i++) {
        Object raw = m.get(labels[i]);
        if (contentTypes[i] == String.class) {
          mappedValues.add(raw != null ? new String((byte[]) raw) : "");
        } else if (contentTypes[i] == Long.class) {
          mappedValues.add(raw != null ? Long.parseLong(new String((byte[]) raw)) : 0L);
        } else if (contentTypes[i] == Double.class) {
          mappedValues.add(raw != null ? Double.parseDouble(new String((byte[]) raw)) : 0);
        } else if (contentTypes[i] == List.class && List.class.isAssignableFrom(raw.getClass())) {
          Class<?> listContents = returnFieldsTypeHints.get(labels[i]);
          List<?> rawList = (List<?>) raw;
          if (listContents != null) {
            if (listContents == String.class) {
              mappedValues.add(
                  rawList.stream().map(e -> e != null ? new String((byte[]) e) : "").toList());
            } else if (listContents == Long.class) {
              mappedValues.add(rawList.stream().map(e -> e != null ? Long.parseLong(new String((byte[]) e)) : 0L)
                  .toList());
            } else if (listContents == Double.class) {
              mappedValues.add(rawList.stream().map(e -> e != null ? Double.parseDouble(new String((byte[]) e)) : 0)
                  .toList());
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
        case 8 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7]);
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
                values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15],
                values[16]);
        case 18 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15],
                values[16], values[17]);
        case 19 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15],
                values[16], values[17], values[18]);
        case 20 ->
            Tuples.of(labels, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15],
                values[16], values[17], values[18], values[19]);
        default -> Tuples.of();
      };
    }).toList();
    return (List<R>) asList;
  }

  private void applyCurrentGroupBy() {
    if (currentGroup != null) {
      applyCurrentReducer();
      aggregation.groupBy(currentGroup);
      currentGroup = null;
    }
  }

  private void createAggregationGroup(MetamodelField<?, ?>... fields) {
    if (fields.length > 0) {
      var aliases = new ArrayList<String>();
      for (MetamodelField<?, ?> mmf : fields) {
        aliases.add(mmf.getSearchAlias());
        returnFieldsTypeHints.put(mmf.getSearchAlias(), mmf.getTargetClass());
      }
      currentGroup = new Group(aliases.stream().map(alias -> String.format("@%s", alias)).toArray(String[]::new));
      returnFields.addAll(aliases);
    }
  }

  private Class<?> getTypeHintForReducer(Reducer cr, MetamodelField<?, ?> field) {
    Class<?> fieldTargetClass = field != null ? field.getTargetClass() : null;
    return switch (cr.getName()) {
      case "COUNT", "COUNT_DISTINCT", "COUNT_DISTINCTISH" -> Long.class;
      case "SUM", "MIN", "MAX", "QUANTILE", "FIRST_VALUE", "TOLIST", "RANDOM_SAMPLE" ->
          fieldTargetClass != null ? fieldTargetClass : String.class;
      case "AVG", "STDDEV" -> Double.class;
      default -> String.class;
    };
  }

}
