package com.redis.om.spring.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying reducer functions in aggregation queries.
 * <p>
 * This annotation is used to define reducer functions that can be applied
 * during aggregation operations in RediSearch queries. Reducers perform
 * calculations or transformations on grouped data.
 * <p>
 * Common use cases include:
 * <ul>
 * <li>Mathematical operations (count, sum, average, min, max)</li>
 * <li>String operations (concatenation, first/last values)</li>
 * <li>Statistical calculations (standard deviation, variance)</li>
 * <li>Custom aggregation functions</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Target(
  { ElementType.METHOD, ElementType.ANNOTATION_TYPE }
)
@Retention(
  RetentionPolicy.RUNTIME
)
public @interface Reducer {
  /**
   * The reducer function to apply during aggregation.
   * <p>
   * This specifies which mathematical or logical operation should be
   * performed on the grouped data during the aggregation phase.
   *
   * @return the reducer function type
   */
  ReducerFunction func();

  /**
   * Arguments to pass to the reducer function.
   * <p>
   * These are the field names or values that the reducer function
   * will operate on. The meaning and number of arguments depends
   * on the specific reducer function being used.
   *
   * @return array of argument strings for the reducer function
   */
  String[] args() default {};

  /**
   * Alias name for the result of the reducer operation.
   * <p>
   * This provides a custom name for the aggregated result field
   * in the query response. If not specified, a default alias
   * will be generated based on the reducer function.
   *
   * @return the alias name for the aggregation result
   */
  String alias() default "";
}
