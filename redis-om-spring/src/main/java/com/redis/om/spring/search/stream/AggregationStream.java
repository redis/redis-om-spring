package com.redis.om.spring.search.stream;

import com.redis.om.spring.annotations.ReducerFunction;
import com.redis.om.spring.metamodel.MetamodelField;
import io.redisearch.AggregationResult;
import org.springframework.data.domain.Sort.Order;

import java.util.List;

public interface AggregationStream<T> {
  AggregationStream<T> load(MetamodelField<?, ?>... fields);

  AggregationStream<T> groupBy(MetamodelField<?, ?>... fields);

  AggregationStream<T> reduce(ReducerFunction reducer);

  AggregationStream<T> apply(String expression, String alias);

  AggregationStream<T> as(String alias);

  AggregationStream<T> sorted(Order... fields);

  AggregationStream<T> sorted(int max, Order ...fields);

  AggregationStream<T> reduce(ReducerFunction reducer, MetamodelField<?, ?> field, String... params);

  AggregationStream<T> limit(int limit);

  AggregationStream<T> limit(int limit, int offset);

  AggregationStream<T> filter(String... filters);

  AggregationResult aggregate();

  <R extends T> List<R> toList(Class<?>... contentTypes);
}
