package com.redis.om.spring.search.stream;

import com.redis.om.spring.annotations.ReducerFunction;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.search.stream.aggregations.filters.AggregationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import redis.clients.jedis.search.aggr.AggregationResult;

import java.time.Duration;
import java.util.List;

public interface AggregationStream<T> {
  AggregationStream<T> load(MetamodelField<?, ?>... fields);

  AggregationStream<T> loadAll();

  AggregationStream<T> groupBy(MetamodelField<?, ?>... fields);

  AggregationStream<T> apply(String expression, String alias);

  AggregationStream<T> as(String alias);

  AggregationStream<T> sorted(Order... fields);

  AggregationStream<T> sorted(int max, Order... fields);

  AggregationStream<T> reduce(ReducerFunction reducer);

  AggregationStream<T> reduce(ReducerFunction reducer, MetamodelField<?, ?> field, Object... params);

  AggregationStream<T> reduce(ReducerFunction reducer, String alias, Object... params);

  AggregationStream<T> limit(int limit);

  AggregationStream<T> limit(int limit, int offset);

  AggregationStream<T> filter(String... filters);

  AggregationStream<T> filter(AggregationFilter... filters);

  AggregationResult aggregate();

  AggregationResult aggregateVerbatim();

  AggregationResult aggregate(Duration timeout);

  AggregationResult aggregateVerbatim(Duration timeout);

  <R extends T> List<R> toList(Class<?>... contentTypes);

  String backingQuery();

  // Cursor API
  AggregationStream<T> cursor(int i, Duration duration);

  <R extends T> Page<R> toList(Pageable pageRequest, Class<?>... contentTypes);

  <R extends T> Page<R> toList(Pageable pageRequest, Duration duration, Class<?>... contentTypes);
}
