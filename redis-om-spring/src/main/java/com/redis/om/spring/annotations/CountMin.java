package com.redis.om.spring.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
public @interface CountMin {
  String name() default "";

  /**
   * Initialization mode for the Count-min Sketch.
   * DIMENSIONS: Initialize by width and depth
   * PROBABILITY: Initialize by error rate and probability
   */
  InitMode initMode() default InitMode.PROBABILITY;

  /**
   * Number of counter in each array. Reduces the error size.
   * Only used when initMode is DIMENSIONS.
   */
  long width() default 0;

  /**
   * Number of counter-arrays. Reduces the probability for an error of a certain size.
   * Only used when initMode is DIMENSIONS.
   */
  long depth() default 0;

  /**
   * Estimate size of error. The error is a percent of total counted items.
   * This affects the width of the sketch.
   * Only used when initMode is PROBABILITY.
   */
  double errorRate() default 0.001;

  /**
   * The desired probability for inflated count. This should be a decimal value between 0 and 1.
   * This affects the depth of the sketch.
   * Only used when initMode is PROBABILITY.
   */
  double probability() default 0.99;

  /**
   * Initialization mode for Count-min Sketch
   */
  enum InitMode {
    DIMENSIONS,
    PROBABILITY
  }
}