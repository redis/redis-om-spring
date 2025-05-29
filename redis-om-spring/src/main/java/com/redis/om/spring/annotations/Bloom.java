package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation for configuring automatic Bloom Filter maintenance on entity fields.
 * <p>
 * When applied to a field in a Redis OM Spring entity, this annotation enables automatic
 * creation and maintenance of a Redis Bloom Filter that tracks the values of that field
 * across all instances of the entity. This provides an efficient way to check for the
 * existence of specific field values without querying the main data store.
 * </p>
 * <p>
 * Bloom filters are probabilistic data structures that can yield false positives but
 * never false negatives. This makes them ideal for preliminary existence checks that
 * can filter out definitely non-existent values before performing more expensive
 * operations.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Document
 * public class User {
 * 
 * @Id
 *     private String id;
 * 
 * @Bloom(capacity = 10000, errorRate = 0.01)
 *                 private String email;
 *                 }
 * 
 *                 // The Bloom filter will be automatically maintained
 *                 // Repository methods like existsByEmail() will use the filter
 *                 }</pre>
 *                 <p>
 *                 The annotation triggers the {@link com.redis.om.spring.bloom.BloomAspect} to
 *                 automatically add values to the configured Bloom filter during save operations
 *                 and enables efficient existence queries.
 *                 </p>
 *
 * @see com.redis.om.spring.bloom.BloomAspect
 * @see com.redis.om.spring.ops.pds.BloomOperations
 * @since 0.1.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface Bloom {
  /**
   * The name of the Bloom filter.
   * <p>
   * If not specified, a default name will be generated based on the entity class
   * and field name. It's recommended to provide explicit names for better control
   * and to avoid naming conflicts.
   * </p>
   *
   * @return the Bloom filter name
   */
  String name() default "";

  /**
   * The desired false positive error rate for the Bloom filter.
   * <p>
   * This value determines the trade-off between space efficiency and accuracy.
   * Lower values result in larger filters but fewer false positives. Typical
   * values range from 0.001 (0.1%) to 0.01 (1%).
   * </p>
   * <p>
   * Examples:
   * <ul>
   * <li>0.001 = 0.1% false positive rate (more accurate, larger size)</li>
   * <li>0.01 = 1% false positive rate (less accurate, smaller size)</li>
   * </ul>
   *
   * @return the error rate as a decimal between 0.0 and 1.0
   */
  double errorRate();

  /**
   * The expected number of unique items that will be added to the Bloom filter.
   * <p>
   * This value is used to optimize the filter's size and hash function count
   * for the specified error rate. Setting this value correctly is important
   * for achieving the desired false positive rate.
   * </p>
   * <p>
   * If the actual number of items exceeds this capacity, the false positive
   * rate will increase beyond the configured error rate.
   * </p>
   *
   * @return the expected capacity (number of items)
   */
  int capacity();
}
