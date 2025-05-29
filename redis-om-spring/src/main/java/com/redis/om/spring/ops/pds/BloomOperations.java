package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.bloom.BFInsertParams;

/**
 * Operations interface for Redis Bloom Filter commands.
 * <p>
 * This interface provides a high-level abstraction for working with Redis Bloom Filters,
 * which are space-efficient probabilistic data structures that can test whether an element
 * is a member of a set. Bloom filters can produce false positives but never false negatives,
 * making them ideal for applications where some false positives are acceptable in exchange
 * for significant space savings.
 *
 * <p>Redis OM Spring automatically integrates Bloom filter operations when entities are
 * annotated with {@link com.redis.om.spring.annotations.Bloom}. This enables automatic
 * maintenance of Bloom filters during entity save operations, allowing for efficient
 * existence checks without querying the main data store.
 *
 * <p>Key features:
 * <ul>
 * <li>Configurable false positive rates and capacity</li>
 * <li>Batch operations for improved performance</li>
 * <li>Automatic filter creation with optimal parameters</li>
 * <li>Integration with Spring's transaction management</li>
 * </ul>
 *
 * @param <K> the type of keys used to identify bloom filters
 * @see com.redis.om.spring.annotations.Bloom
 * @see com.redis.om.spring.bloom.BloomAspect
 * @since 0.1.0
 */
public interface BloomOperations<K> {
  /**
   * Reserve a bloom filter.
   *
   * @param name         The key of the filter
   * @param initCapacity Optimize for this many items
   * @param errorRate    The desired rate of false positives
   *                     <p>
   *                     Note that if a filter is not reserved, a new one is created when is called.
   */
  void createFilter(K name, long initCapacity, double errorRate);

  /**
   * Adds an item to the filter
   *
   * @param name  The name of the filter
   * @param value The value to add to the filter
   * @return true if the item was not previously in the filter.
   */
  boolean add(K name, String value);

  /**
   * add one or more items to the bloom filter, by default creating it if it does not yet exist
   *
   * @param name    The name of the filter
   * @param options {@link BFInsertParams}
   * @param items   items to add to the filter
   * @return List of booleans, true for each successful insertion
   */
  List<Boolean> insert(K name, BFInsertParams options, String... items);

  /**
   * Add one or more items to a filter
   *
   * @param name   Name of the filter
   * @param values values to add to the filter.
   * @return An array of booleans of the same length as the number of values.
   *         Each boolean values indicates whether the corresponding element was previously in the
   *         filter or not. A true value means the item did not previously exist, whereas a
   *         false value means it may have previously existed.
   */
  List<Boolean> addMulti(K name, String... values);

  /**
   * Check if an item exists in the filter
   *
   * @param name  Name (key) of the filter
   * @param value Value to check for
   * @return true if the item may exist in the filter, false if the item does not exist in the filter
   */
  boolean exists(K name, String value);

  /**
   * Check if one or more items exist in the filter
   *
   * @param name   Name of the filter to check
   * @param values values to check for
   * @return An array of booleans. A true value means the corresponding value may exist, false means it does not exist
   */
  List<Boolean> existsMulti(K name, String... values);

  /**
   * Get information about the filter
   *
   * @param name the name of the filter
   * @return Return information
   */
  Map<String, Object> info(K name);
}
