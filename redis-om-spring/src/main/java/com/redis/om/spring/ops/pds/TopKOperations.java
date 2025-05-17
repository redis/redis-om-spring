package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

public interface TopKOperations<K> {
  /**
   * Create a new TopK filter.
   *
   * @param key  The key of the filter
   * @param topk Number of top items to keep
   * @return Status string reply
   */
  String createFilter(K key, long topk);

  /**
   * Create a new TopK filter with additional parameters.
   *
   * @param key   The key of the filter
   * @param topk  Number of top items to keep
   * @param width Number of counters kept in each array
   * @param depth Number of arrays
   * @param decay The probability of reducing a counter in an occupied bucket
   * @return Status string reply
   */
  String createFilter(K key, long topk, long width, long depth, double decay);

  /**
   * Add one or more items to the filter.
   *
   * @param key   The key of the filter
   * @param items Items to add to the filter
   * @return List of items dropped from the filter
   */
  List<String> add(K key, String... items);

  /**
   * Increase the score of an item by increment.
   *
   * @param key       The key of the filter
   * @param item      Item to increment
   * @param increment Increment by this much
   * @return Item dropped from the filter, or null if no item was dropped
   */
  String incrementBy(K key, String item, long increment);

  /**
   * Increase the score of multiple items by their increments.
   *
   * @param key              The key of the filter
   * @param itemIncrementMap Map of item to increment
   * @return List of items dropped from the filter
   */
  List<String> incrementBy(K key, Map<String, Long> itemIncrementMap);

  /**
   * Check if items exist in the filter.
   *
   * @param key   The key of the filter
   * @param items Items to check
   * @return List of boolean values indicating if items exist in the filter
   */
  List<Boolean> query(K key, String... items);

  /**
   * Return the top k items in the filter.
   *
   * @param key The key of the filter
   * @return List of items
   */
  List<String> list(K key);

  /**
   * Return the top k items with their respective counts.
   *
   * @param key The key of the filter
   * @return Map of items to their counts
   */
  Map<String, Long> listWithCount(K key);

  /**
   * Get information about the filter.
   *
   * @param key The key of the filter
   * @return Map of information about the filter
   */
  Map<String, Object> info(K key);
}
