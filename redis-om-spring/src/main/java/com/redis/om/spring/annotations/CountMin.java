package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation for configuring automatic Count-Min Sketch maintenance on entity fields.
 * <p>
 * When applied to a field in a Redis OM Spring entity, this annotation enables automatic
 * creation and maintenance of a Redis Count-Min Sketch that tracks the frequency of
 * values in that field across all instances of the entity. Count-Min Sketches are
 * probabilistic data structures that provide approximate frequency counts with
 * sub-linear space requirements.
 * </p>
 * <p>
 * Count-Min Sketches can answer "how many times has this item been seen?" queries
 * with high accuracy while using significantly less memory than exact counting
 * would require. They may overestimate frequencies but never underestimate them.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Document
 * public class PageView {
 * 
 * @Id
 *     private String id;
 * 
 * @CountMin(initMode = InitMode.PROBABILITY, errorRate = 0.001, probability = 0.99)
 *                    private String pagePath;
 *                    }
 * 
 *                    // The Count-Min Sketch will automatically track page path frequencies
 *                    // Repository methods like countByPagePath() will use the sketch
 *                    }</pre>
 *
 * @see com.redis.om.spring.countmin.CountMinAspect
 * @see com.redis.om.spring.ops.pds.CountMinSketchOperations
 * @since 0.1.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface CountMin {
  /**
   * The name of the Count-Min Sketch.
   * <p>
   * If not specified, a default name will be generated based on the entity class
   * and field name.
   * </p>
   *
   * @return the sketch name
   */
  String name() default "";

  /**
   * The initialization mode for the Count-Min Sketch.
   * <p>
   * Determines how the sketch dimensions are calculated:
   * <ul>
   * <li>DIMENSIONS: Use explicit width and depth values</li>
   * <li>PROBABILITY: Calculate dimensions from error rate and probability</li>
   * </ul>
   *
   * @return the initialization mode
   */
  InitMode initMode() default InitMode.PROBABILITY;

  /**
   * Number of counters in each array.
   * <p>
   * Only used when initMode is DIMENSIONS. A larger width reduces the error size
   * but increases memory usage.
   * </p>
   *
   * @return the width (number of counters per array)
   */
  long width() default 0;

  /**
   * Number of counter arrays.
   * <p>
   * Only used when initMode is DIMENSIONS. A larger depth reduces the probability
   * of error but increases memory usage.
   * </p>
   *
   * @return the depth (number of arrays)
   */
  long depth() default 0;

  /**
   * Estimate size of error as a percentage of total counted items.
   * <p>
   * Only used when initMode is PROBABILITY. This affects the width of the sketch.
   * Lower values provide more accuracy but require more memory.
   * </p>
   *
   * @return the error rate as a decimal (e.g., 0.001 = 0.1% error)
   */
  double errorRate() default 0.001;

  /**
   * The desired probability for the error bound.
   * <p>
   * Only used when initMode is PROBABILITY. This should be a decimal value between
   * 0 and 1, representing the confidence level that the error will not exceed the
   * specified error rate. This affects the depth of the sketch.
   * </p>
   *
   * @return the probability as a decimal (e.g., 0.99 = 99% confidence)
   */
  double probability() default 0.99;

  /**
   * Initialization mode for Count-min Sketch
   */
  enum InitMode {
    /**
     * Initialize by explicit width and depth dimensions.
     */
    DIMENSIONS,
    /**
     * Initialize by error rate and probability parameters.
     */
    PROBABILITY
  }
}