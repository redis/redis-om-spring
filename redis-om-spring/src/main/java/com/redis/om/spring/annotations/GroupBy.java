package com.redis.om.spring.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define grouping and aggregation operations in Redis search queries.
 * <p>
 * This annotation is used to specify how search results should be grouped and
 * what reduction operations should be applied to each group. It supports
 * Redis aggregation pipeline operations for advanced data analysis.
 * </p>
 * 
 * @since 1.0.0
 * @see Reducer
 */
@Target(
  { ElementType.METHOD, ElementType.ANNOTATION_TYPE }
)
@Retention(
  RetentionPolicy.RUNTIME
)
public @interface GroupBy {
  /**
   * Specifies the field names to group by.
   * These fields will be used as grouping keys in the aggregation pipeline.
   * Multiple fields can be specified to create composite grouping keys.
   * 
   * @return an array of field names for grouping
   */
  String[] properties() default {};

  /**
   * Specifies the reduction operations to apply to each group.
   * These operations define how values within each group should be aggregated,
   * such as counting, summing, averaging, etc.
   * 
   * @return an array of reduction operations
   * @see Reducer
   */
  Reducer[] reduce() default {};
}
