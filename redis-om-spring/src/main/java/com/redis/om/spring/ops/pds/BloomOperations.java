package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.bloom.BFInsertParams;

public interface BloomOperations<K> {
  /**
   * Reserve a bloom filter.
   * @param name The key of the filter
   * @param initCapacity Optimize for this many items
   * @param errorRate The desired rate of false positives
   *
   * Note that if a filter is not reserved, a new one is created when is called.
   */
  void createFilter(K name, long initCapacity, double errorRate);

  /**
   * Adds an item to the filter
   * @param name The name of the filter
   * @param value The value to add to the filter
   * @return true if the item was not previously in the filter.
   */
  boolean add(K name, String value);

  /**
   * add one or more items to the bloom filter, by default creating it if it does not yet exist
   *
   * @param name The name of the filter
   * @param options {@link BFInsertParams}
   * @param items items to add to the filter
   * @return array of booleans, true for each succesful insertion
   */
  List<Boolean> insert(K name, BFInsertParams options, String... items);

  /**
   * Add one or more items to a filter
   * @param name Name of the filter
   * @param values values to add to the filter.
   * @return An array of booleans of the same length as the number of values.
   * Each boolean values indicates whether the corresponding element was previously in the
   * filter or not. A true value means the item did not previously exist, whereas a
   * false value means it may have previously existed.
   */
  List<Boolean> addMulti(K name, String ...values);

  /**
   * Check if an item exists in the filter
   * @param name Name (key) of the filter
   * @param value Value to check for
   * @return true if the item may exist in the filter, false if the item does not exist in the filter
   */
  boolean exists(K name, String value);

  /**
   * Check if one or more items exist in the filter
   * @param name Name of the filter to check
   * @param values values to check for
   * @return An array of booleans. A true value means the corresponding value may exist, false means it does not exist
   */
  List<Boolean> existsMulti(K name, String ...values);

  /**
   * Get information about the filter
   * @param name the name of the filter
   * @return Return information
   */
  Map<String, Object> info(K name);
}
