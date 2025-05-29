package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation for defining custom RediSearch queries on repository methods.
 * <p>
 * This annotation allows developers to specify custom search queries using RediSearch syntax
 * instead of relying on method name-based query derivation. It provides fine-grained control
 * over search operations including query text, return fields, pagination, and sorting.
 * </p>
 * <p>
 * The annotation can be applied to repository methods to override the default query behavior
 * and execute custom RediSearch queries against Redis indexes.
 * </p>
 *
 * @see com.redis.om.spring.repository.RedisDocumentRepository
 * @see com.redis.om.spring.repository.RedisEnhancedRepository
 * @since 0.1.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.METHOD, ElementType.ANNOTATION_TYPE }
)
public @interface Query {
  /**
   * The RediSearch query string to execute.
   * <p>
   * This should be a valid RediSearch query expression. Use "*" to match all documents.
   * Supports advanced RediSearch syntax including field-specific searches, boolean operations,
   * range queries, and fuzzy matching.
   * </p>
   *
   * @return the RediSearch query string
   */
  String value() default "*";

  /**
   * Specifies which fields to return in the search results.
   * <p>
   * When specified, only these fields will be included in the returned documents.
   * If empty (default), all fields are returned. This can improve performance
   * when only specific fields are needed.
   * </p>
   *
   * @return array of field names to return
   */
  String[] returnFields() default {};

  /**
   * The number of documents to skip in the result set.
   * <p>
   * Used for pagination. When combined with limit, allows for efficient
   * paging through large result sets. Default value indicates no offset.
   * </p>
   *
   * @return the offset for pagination
   */
  int offset() default Integer.MIN_VALUE;

  /**
   * The maximum number of documents to return.
   * <p>
   * Used for pagination and result limiting. Default value indicates no limit.
   * Should be used with offset for proper pagination implementation.
   * </p>
   *
   * @return the maximum number of results to return
   */
  int limit() default Integer.MIN_VALUE;

  /**
   * The field name to sort results by.
   * <p>
   * Specifies which field should be used for sorting the search results.
   * The field must be sortable (indexed with sortable option). If empty,
   * results are returned in their natural order.
   * </p>
   *
   * @return the field name for sorting
   */
  String sortBy() default "";

  /**
   * Whether to sort results in ascending order.
   * <p>
   * When true (default), results are sorted in ascending order.
   * When false, results are sorted in descending order.
   * Only effective when sortBy is specified.
   * </p>
   *
   * @return true for ascending sort, false for descending sort
   */
  boolean sortAscending() default true;
}
