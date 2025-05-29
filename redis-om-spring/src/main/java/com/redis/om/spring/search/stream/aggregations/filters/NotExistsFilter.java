package com.redis.om.spring.search.stream.aggregations.filters;

/**
 * Aggregation filter for excluding documents where a specified field does not exist.
 * This filter is used in RediSearch aggregations to filter out documents that
 * do not have the specified field present, effectively creating a "field exists" condition.
 */
public class NotExistsFilter implements AggregationFilter {
  private final String field;

  /**
   * Constructs a NotExistsFilter for the specified field.
   *
   * @param field the name of the field that should not exist in filtered documents
   */
  public NotExistsFilter(String field) {
    this.field = field;
  }

  /**
   * Generates the RediSearch filter expression for the "not exists" condition.
   * 
   * @return the filter expression in the format "!exists(@fieldname)"
   */
  public String getFilter() {
    return "!exists(@" + this.getField() + ")";
  }

  /**
   * Gets the field name that should not exist in filtered documents.
   *
   * @return the field name
   */
  public String getField() {
    return this.field;
  }
}
