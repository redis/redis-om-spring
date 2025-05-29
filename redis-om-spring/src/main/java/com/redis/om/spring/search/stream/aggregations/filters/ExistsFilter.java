package com.redis.om.spring.search.stream.aggregations.filters;

/**
 * An aggregation filter that checks for the existence of a field.
 * This filter generates a Redis query that verifies if the specified field exists.
 */
public class ExistsFilter implements AggregationFilter {
  private final String field;

  /**
   * Constructs a new ExistsFilter.
   *
   * @param field the name of the field to check for existence
   */
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
