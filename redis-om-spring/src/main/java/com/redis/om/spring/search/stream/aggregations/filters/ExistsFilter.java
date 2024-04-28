package com.redis.om.spring.search.stream.aggregations.filters;

public class ExistsFilter implements AggregationFilter {
  private final String field;

  public ExistsFilter(String field) {
    this.field = field;
  }

  public String getFilter() {
    return "exists(@" + this.getField() + ")";
  }

  public String getField() {
    return this.field;
  }
}
