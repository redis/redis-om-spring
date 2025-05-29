package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

/**
 * Interface for Redis TopK probabilistic data structure operations.
 * 
 * <p>TopK is a probabilistic data structure in Redis that maintains a list of the
 * top-K most frequent items in a data stream. It uses a Count-Min Sketch with
 * a min-heap to efficiently track the most frequent elements while using
 * minimal memory. This makes it ideal for scenarios where exact counting
 * would be too memory-intensive.
 * 
 * <p>Key characteristics of TopK:
 * <ul>
 * <li>Space-efficient: Uses sub-linear space relative to the number of unique items</li>
 * <li>Approximate: May over-count but never under-counts item frequencies</li>
 * <li>Dynamic: Automatically evicts less frequent items as new items are added</li>
 * <li>Query-friendly: Supports fast membership queries and top-K retrieval</li>
 * </ul>
 * 
 * <p>Common use cases include:
 * <ul>
 * <li>Tracking most popular products, pages, or search queries</li>
 * <li>Identifying trending topics or hashtags in social media</li>
 * <li>Monitoring most frequent errors or events in logging systems</li>
 * <li>Finding heavy hitters in network traffic analysis</li>
 * <li>Real-time analytics for high-volume data streams</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * TopKOperations<String> topK = redisModulesOperations.opsForTopK();
 * 
 * // Create a TopK filter to track top 10 items
 * topK.createFilter("popular-products", 10);
 * 
 * // Add items (returns items that were evicted)
 * List<String> evicted = topK.add("popular-products", "laptop", "phone", "tablet");
 * 
 * // Increment item counts
 * topK.incrementBy("popular-products", "laptop", 5);
 * 
 * // Get the current top items with their counts
 * Map<String, Long> topItems = topK.listWithCount("popular-products");
 * }</pre>
 * 
 * @param <K> The type of keys used to identify TopK filters
 * @see <a href="https://redis.io/docs/stack/bloom/">Redis TopK Documentation</a>
 */
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
