package com.redis.om.spring.search.stream;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;

import com.redis.om.spring.annotations.ReducerFunction;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.search.stream.aggregations.filters.AggregationFilter;

import redis.clients.jedis.search.aggr.AggregationResult;

/**
 * A fluent API for building and executing Redis aggregation queries.
 * AggregationStream provides a chainable interface for constructing complex aggregation
 * pipelines that can perform operations like grouping, filtering, sorting, and applying
 * reducer functions on Redis data.
 * 
 * <p>The aggregation stream follows a builder pattern where each method returns a new
 * stream instance with the additional operation applied. This allows for flexible
 * composition of aggregation operations.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * List<ProductStats> results = entityStream
 *     .load(Product$.category, Product$.price)
 *     .filter("@price:[100 500]")
 *     .groupBy(Product$.category)
 *     .reduce(ReducerFunction.SUM, Product$.price)
 *     .sorted(Order.desc("category"))
 *     .limit(10)
 *     .toList(ProductStats.class);
 * }</pre>
 * 
 * <p>Key features:</p>
 * <ul>
 * <li>Fluent API for building aggregation pipelines</li>
 * <li>Support for field loading, filtering, and grouping</li>
 * <li>Rich set of reducer functions for data analysis</li>
 * <li>Pagination and sorting capabilities</li>
 * <li>Cursor-based processing for large datasets</li>
 * <li>Timeout support for long-running operations</li>
 * </ul>
 * 
 * @param <T> the entity type being aggregated
 * 
 * @since 1.0
 * @see AggregationResult
 * @see MetamodelField
 * @see ReducerFunction
 */
public interface AggregationStream<T> {
  /**
   * Loads specific fields from the documents for processing in the aggregation pipeline.
   * Only the specified fields will be available for subsequent operations.
   * 
   * @param fields the metamodel fields to load into the aggregation pipeline
   * @return a new AggregationStream with the specified fields loaded
   * @throws IllegalArgumentException if fields is empty or contains null elements
   */
  AggregationStream<T> load(MetamodelField<?, ?>... fields);

  /**
   * Loads all available fields from the documents for processing in the aggregation pipeline.
   * This is equivalent to SELECT * in SQL aggregations.
   * 
   * @return a new AggregationStream with all fields loaded
   */
  AggregationStream<T> loadAll();

  /**
   * Groups the aggregation results by the specified fields.
   * This operation is similar to GROUP BY in SQL and is typically followed by reducer functions.
   * 
   * @param fields the metamodel fields to group by
   * @return a new AggregationStream with grouping applied
   * @throws IllegalArgumentException if fields is empty or contains null elements
   */
  AggregationStream<T> groupBy(MetamodelField<?, ?>... fields);

  /**
   * Applies a custom expression to the aggregation pipeline and assigns it an alias.
   * The expression can perform calculations, transformations, or other operations on the data.
   * 
   * @param expression the RediSearch expression to apply
   * @param alias      the alias name for the result of the expression
   * @return a new AggregationStream with the expression applied
   * @throws IllegalArgumentException if expression or alias is null or empty
   */
  AggregationStream<T> apply(String expression, String alias);

  /**
   * Assigns an alias to the current aggregation stage.
   * This can be useful for referencing the results of complex operations in subsequent stages.
   * 
   * @param alias the alias name to assign
   * @return a new AggregationStream with the alias applied
   * @throws IllegalArgumentException if alias is null or empty
   */
  AggregationStream<T> as(String alias);

  /**
   * Sorts the aggregation results by the specified fields and ordering.
   * Multiple sort orders can be applied, with earlier orders taking precedence.
   * 
   * @param fields the sort orders to apply
   * @return a new AggregationStream with sorting applied
   * @throws IllegalArgumentException if fields is empty or contains null elements
   */
  AggregationStream<T> sorted(Order... fields);

  /**
   * Sorts the aggregation results by the specified fields with a maximum number of results.
   * This combines sorting with a limit operation for efficiency.
   * 
   * @param max    the maximum number of results to return after sorting
   * @param fields the sort orders to apply
   * @return a new AggregationStream with sorting and limiting applied
   * @throws IllegalArgumentException if max is negative or fields contains null elements
   */
  AggregationStream<T> sorted(int max, Order... fields);

  /**
   * Applies a reducer function to the aggregated data.
   * The reducer performs calculations like SUM, COUNT, AVG, etc. on the grouped data.
   * 
   * @param reducer the reducer function to apply
   * @return a new AggregationStream with the reducer applied
   * @throws IllegalArgumentException if reducer is null
   */
  AggregationStream<T> reduce(ReducerFunction reducer);

  /**
   * Applies a reducer function to a specific field with optional parameters.
   * 
   * @param reducer the reducer function to apply
   * @param field   the field to apply the reducer to
   * @param params  additional parameters for the reducer function
   * @return a new AggregationStream with the reducer applied
   * @throws IllegalArgumentException if reducer or field is null
   */
  AggregationStream<T> reduce(ReducerFunction reducer, MetamodelField<?, ?> field, Object... params);

  /**
   * Applies a reducer function with a custom alias and optional parameters.
   * 
   * @param reducer the reducer function to apply
   * @param alias   the alias for the reducer result
   * @param params  additional parameters for the reducer function
   * @return a new AggregationStream with the reducer applied
   * @throws IllegalArgumentException if reducer or alias is null
   */
  AggregationStream<T> reduce(ReducerFunction reducer, String alias, Object... params);

  /**
   * Limits the number of results returned by the aggregation.
   * 
   * @param limit the maximum number of results to return
   * @return a new AggregationStream with the limit applied
   * @throws IllegalArgumentException if limit is negative
   */
  AggregationStream<T> limit(int limit);

  /**
   * Limits the number of results and applies an offset for pagination.
   * <p>
   * This method controls the pagination of aggregation results by specifying
   * how many results to skip (offset) and how many to return (limit).
   * </p>
   * <p>
   * Example usage:
   * <pre>{@code
   * // Skip the first 10 results and return the next 5
   * stream.limit(10, 5)  // offset=10, limit=5
   * }</pre>
   * 
   * @param offset the number of results to skip before starting to return results (0-based)
   * @param limit  the maximum number of results to return
   * @return a new AggregationStream with the limit and offset applied
   * @throws IllegalArgumentException if limit or offset is negative
   */
  AggregationStream<T> limit(int offset, int limit);

  /**
   * Applies filter expressions to the aggregation pipeline.
   * Filters restrict the data processed by subsequent aggregation operations.
   * 
   * @param filters the filter expressions in RediSearch syntax
   * @return a new AggregationStream with the filters applied
   * @throws IllegalArgumentException if filters is empty or contains null elements
   */
  AggregationStream<T> filter(String... filters);

  /**
   * Applies structured filter objects to the aggregation pipeline.
   * 
   * @param filters the aggregation filter objects to apply
   * @return a new AggregationStream with the filters applied
   * @throws IllegalArgumentException if filters is empty or contains null elements
   */
  AggregationStream<T> filter(AggregationFilter... filters);

  /**
   * Executes the aggregation pipeline and returns the raw results.
   * 
   * @return the aggregation results from Redis
   * @throws RuntimeException if the aggregation execution fails
   */
  AggregationResult aggregate();

  /**
   * Executes the aggregation pipeline in verbatim mode, which preserves the exact
   * query syntax without Redis performing query expansions or modifications.
   * 
   * @return the aggregation results from Redis
   * @throws RuntimeException if the aggregation execution fails
   */
  AggregationResult aggregateVerbatim();

  /**
   * Executes the aggregation pipeline with a specified timeout.
   * 
   * @param timeout the maximum time to wait for the aggregation to complete
   * @return the aggregation results from Redis
   * @throws RuntimeException if the aggregation execution fails or times out
   */
  AggregationResult aggregate(Duration timeout);

  /**
   * Executes the aggregation pipeline in verbatim mode with a specified timeout.
   * 
   * @param timeout the maximum time to wait for the aggregation to complete
   * @return the aggregation results from Redis
   * @throws RuntimeException if the aggregation execution fails or times out
   */
  AggregationResult aggregateVerbatim(Duration timeout);

  /**
   * Executes the aggregation and converts the results to a list of typed objects.
   * 
   * @param <R>          the result type
   * @param contentTypes the classes to use for result deserialization
   * @return a list of typed aggregation results
   * @throws RuntimeException if the aggregation execution or conversion fails
   */
  <R extends T> List<R> toList(Class<?>... contentTypes);

  /**
   * Executes the aggregation and converts the results to a list of projection objects.
   * The projection interface should define getter methods for the fields to include.
   * IDs are not automatically included - add getId() to the projection interface if needed.
   * 
   * @param <P>             the projection type
   * @param projectionClass the projection interface class
   * @return a list of projection instances with the aggregated data
   * @throws RuntimeException if the aggregation execution or projection creation fails
   */
  <P> List<P> toProjection(Class<P> projectionClass);

  /**
   * Executes the aggregation and converts the results to a list of maps.
   * Each map contains field names as keys and field values as values.
   * By default, entity IDs are included in the results.
   * 
   * @return a list of maps containing the aggregated data with IDs included
   * @throws RuntimeException if the aggregation execution fails
   */
  List<Map<String, Object>> toMaps();

  /**
   * Executes the aggregation and converts the results to a list of maps.
   * Each map contains field names as keys and field values as values.
   * 
   * @param includeId whether to include entity IDs in the results
   * @return a list of maps containing the aggregated data
   * @throws RuntimeException if the aggregation execution fails
   */
  List<Map<String, Object>> toMaps(boolean includeId);

  /**
   * Returns the underlying RediSearch query that would be executed.
   * This is useful for debugging and understanding the generated query.
   * 
   * @return the RediSearch query string
   */
  String backingQuery();

  /**
   * Configures cursor-based iteration for processing large result sets.
   * This enables efficient processing of large aggregation results by fetching
   * results in batches.
   * 
   * @param count    the number of results to fetch per batch
   * @param duration the timeout for each cursor operation
   * @return a new AggregationStream configured for cursor-based processing
   * @throws IllegalArgumentException if count is negative or duration is null
   */
  AggregationStream<T> cursor(int count, Duration duration);

  /**
   * Executes the aggregation with pagination and converts the results to a typed page.
   * 
   * @param <R>          the result type
   * @param pageRequest  the pagination parameters
   * @param contentTypes the classes to use for result deserialization
   * @return a paginated result containing typed aggregation results
   * @throws RuntimeException if the aggregation execution or conversion fails
   */
  <R extends T> Page<R> toList(Pageable pageRequest, Class<?>... contentTypes);

  /**
   * Executes the aggregation with pagination and timeout, converting results to a typed page.
   * 
   * @param <R>          the result type
   * @param pageRequest  the pagination parameters
   * @param duration     the timeout for the aggregation operation
   * @param contentTypes the classes to use for result deserialization
   * @return a paginated result containing typed aggregation results
   * @throws RuntimeException if the aggregation execution or conversion fails or times out
   */
  <R extends T> Page<R> toList(Pageable pageRequest, Duration duration, Class<?>... contentTypes);
}
