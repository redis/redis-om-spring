package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation for declaring Redis aggregation queries on repository methods.
 * This annotation provides a declarative way to define complex aggregation
 * operations using RediSearch's aggregation capabilities.
 * 
 * <p>Aggregation queries allow you to process and analyze data directly in Redis,
 * including operations like grouping, filtering, applying functions, and sorting.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Aggregation(
 * value = "@category:{electronics}",
 * groupBy = @GroupBy(properties = "brand"),
 * apply = @Apply(expression = "count()", alias = "total"),
 * sortBy = @SortBy(field = "total", direction = SortDirection.DESC),
 * limit = 10
 * )
 * List<AggregationResult> getTopBrandsByCategory();
 * }
 * </pre>
 * 
 * @see Apply
 * @see GroupBy
 * @see Load
 * @see SortBy
 * @since 1.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.METHOD, ElementType.ANNOTATION_TYPE }
)
public @interface Aggregation {
  /**
   * The search query string or pattern to filter the dataset.
   * Uses RediSearch query syntax. Defaults to "*" (all documents).
   * 
   * @return the search query string
   */
  String value() default "*";

  /**
   * Whether to use verbatim mode for the query.
   * When true, the query string is used as-is without any transformations.
   * 
   * @return true if verbatim mode should be used, false otherwise
   */
  boolean verbatim() default false;

  /**
   * Fields to load from the documents during aggregation.
   * If not specified, only the document ID is loaded by default.
   * 
   * @return array of Load annotations specifying which fields to load
   */
  Load[] load() default {};

  /**
   * Timeout for the aggregation query in milliseconds.
   * If not specified (Long.MIN_VALUE), uses the default timeout.
   * 
   * @return the timeout value in milliseconds
   */
  long timeout() default Long.MIN_VALUE;

  /**
   * Mathematical expressions to apply during aggregation.
   * These can create computed fields or perform calculations on existing fields.
   * 
   * @return array of Apply annotations defining expressions to evaluate
   */
  Apply[] apply() default {};

  /**
   * Maximum number of results to return.
   * If not specified (Integer.MIN_VALUE), no limit is applied.
   * 
   * @return the maximum number of results
   */
  int limit() default Integer.MIN_VALUE;

  /**
   * Number of results to skip before returning results.
   * Used for pagination in combination with limit.
   * If not specified (Integer.MIN_VALUE), no offset is applied.
   * 
   * @return the number of results to skip
   */
  int offset() default Integer.MIN_VALUE;

  /**
   * Additional filter expressions to apply to the aggregation results.
   * These are applied after grouping and other operations.
   * 
   * @return array of filter expressions
   */
  String[] filter() default {};

  /**
   * Grouping specifications for the aggregation.
   * Defines how to group the results and what reducers to apply.
   * 
   * @return array of GroupBy annotations defining grouping operations
   */
  GroupBy[] groupBy() default {};

  /**
   * Sorting specifications for the aggregation results.
   * Defines the order in which results should be returned.
   * 
   * @return array of SortBy annotations defining sort criteria
   */
  SortBy[] sortBy() default {};

  /**
   * Maximum number of elements to sort.
   * Used to optimize sorting when you only need the top N results.
   * If not specified (Integer.MIN_VALUE), all results are sorted.
   * 
   * @return the maximum number of elements to sort
   */
  int sortByMax() default Integer.MIN_VALUE;
}
