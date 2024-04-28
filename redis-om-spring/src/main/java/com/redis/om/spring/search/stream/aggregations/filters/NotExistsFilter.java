package com.redis.om.spring.search.stream.aggregations.filters;

public class NotExistsFilter implements AggregationFilter {
  private final String field;

  public NotExistsFilter(String field) {
    this.field = field;
  }

  public String getFilter() {
    return "!exists(@" + this.getField() + ")";
  }

  public String getField() {
    return this.field;
  }
}
