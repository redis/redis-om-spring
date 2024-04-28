package com.redis.om.spring.search.stream.aggregations.filters;

public interface AggregationFilter {
  String getFilter();

  String getField();
}
