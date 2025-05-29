package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation for configuring automatic Cuckoo Filter maintenance on entity fields.
 * <p>
 * When applied to a field in a Redis OM Spring entity, this annotation enables automatic
 * creation and maintenance of a Redis Cuckoo Filter that tracks the values of that field
 * across all instances of the entity. Cuckoo Filters are space-efficient probabilistic
 * data structures that support approximate membership testing, similar to Bloom filters
 * but with support for deletions.
 * </p>
 * <p>
 * Cuckoo filters provide the following advantages over Bloom filters:
 * <ul>
 * <li>Support for item deletion</li>
 * <li>Better space efficiency at higher capacities</li>
 * <li>Bounded false positive rates</li>
 * <li>No false negatives</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Document
 * public class Session {
 * 
 * @Id
 *     private String id;
 * 
 * @Cuckoo(capacity = 10000, bucketSize = 4, maxIterations = 500)
 *                  private String sessionToken;
 *                  }
 * 
 *                  // The Cuckoo filter will be automatically maintained
 *                  // Repository methods like existsBySessionToken() will use the filter
 *                  }</pre>
 *
 * @see com.redis.om.spring.cuckoo.CuckooAspect
 * @see com.redis.om.spring.ops.pds.CuckooFilterOperations
 * @since 0.1.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface Cuckoo {
  /**
   * The name of the Cuckoo filter.
   * <p>
   * If not specified, a default name will be generated based on the entity class
   * and field name.
   * </p>
   *
   * @return the Cuckoo filter name
   */
  String name() default "";

  /**
   * The expected number of unique items that will be added to the Cuckoo filter.
   * <p>
   * This value determines the size of the filter and should be set based on the
   * expected number of unique values for the annotated field.
   * </p>
   *
   * @return the expected capacity (number of items)
   */
  int capacity();

  /**
   * The number of entries that each bucket can hold.
   * <p>
   * Higher bucket sizes can improve the filter's load factor but may increase
   * the false positive rate. Typical values are 2, 4, or 8.
   * </p>
   *
   * @return the bucket size (default: 2)
   */
  int bucketSize() default 2;

  /**
   * The maximum number of iterations for resolving hash collisions.
   * <p>
   * When inserting an item causes collisions, the filter will attempt to
   * relocate existing items for up to this many iterations. Higher values
   * improve insertion success rates but may impact performance.
   * </p>
   *
   * @return the maximum number of iterations (default: 20)
   */
  int maxIterations() default 20;

  /**
   * The expansion factor for the filter when it reaches capacity.
   * <p>
   * This parameter controls how the filter grows when it becomes full.
   * A value of 1 means no automatic expansion.
   * </p>
   *
   * @return the expansion factor (default: 1)
   */
  int expansion() default 1;
}
