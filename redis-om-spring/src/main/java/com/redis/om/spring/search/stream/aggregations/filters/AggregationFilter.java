package com.redis.om.spring.search.stream.aggregations.filters;

/**
 * Interface representing a filter that can be applied during Redis aggregation operations.
 * An AggregationFilter combines a field specification with filter criteria to constrain
 * the data processed during aggregation queries.
 * 
 * <p>Aggregation filters are used to restrict the dataset before performing aggregation
 * operations such as grouping, sorting, or applying reducers. They provide a way to
 * apply WHERE-like conditions to aggregation pipelines.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * AggregationFilter filter = new FieldFilter("price", "@price:[100 500]");
 * stream.filter(filter).groupBy(Product$.category).aggregate();
 * }</pre>
 * 
 * @since 1.0
 * @see com.redis.om.spring.search.stream.AggregationStream#filter(AggregationFilter...)
 */
public interface AggregationFilter {
  /**
   * Returns the filter expression to be applied during aggregation.
   * The filter expression should be in RediSearch query syntax format.
   * 
   * @return the filter expression string, never null
   */
  String getFilter();

  /**
   * Returns the field name that this filter applies to.
   * This identifies which field in the document should be filtered.
   * 
   * @return the field name string, never null
   */
  String getField();
}
