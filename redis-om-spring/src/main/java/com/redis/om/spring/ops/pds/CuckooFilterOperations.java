package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.bloom.CFInsertParams;
import redis.clients.jedis.bloom.CFReserveParams;

/**
 * Operations interface for Redis Cuckoo Filter commands.
 * <p>
 * This interface provides a high-level abstraction for working with Redis Cuckoo Filters,
 * which are probabilistic data structures that support approximate membership testing
 * with the ability to delete items. Cuckoo filters are an improvement over Bloom filters
 * in scenarios where item deletion is required while maintaining space efficiency.
 * </p>
 * <p>
 * Key features of Cuckoo Filters:
 * <ul>
 * <li>Support for item deletion (unlike Bloom filters)</li>
 * <li>No false negatives</li>
 * <li>Bounded false positive rates</li>
 * <li>Better space efficiency than Bloom filters for large datasets</li>
 * <li>Support for exact frequency counting of individual items</li>
 * </ul>
 * <p>
 * Redis OM Spring automatically integrates Cuckoo filter operations when entities are
 * annotated with {@link com.redis.om.spring.annotations.Cuckoo}. This enables automatic
 * maintenance of Cuckoo filters during entity save and delete operations.
 *
 *
 * @param <K> the type of keys used to identify cuckoo filters
 * @see com.redis.om.spring.annotations.Cuckoo
 * @see com.redis.om.spring.cuckoo.CuckooAspect
 * @since 0.1.0
 */
public interface CuckooFilterOperations<K> {
  /**
   * Creates a new Cuckoo filter with the specified capacity.
   * <p>
   * This method initializes a new Cuckoo filter that can efficiently store
   * approximately the specified number of items. The actual capacity may be
   * adjusted internally to optimize performance and maintain the filter's
   * mathematical properties.
   * </p>
   *
   * @param key      the unique identifier for the filter
   * @param capacity the expected number of items to be stored
   * @throws IllegalArgumentException if capacity is less than or equal to 0
   */
  void createFilter(String key, long capacity);

  /**
   * Creates a new Cuckoo filter with the specified capacity and custom parameters.
   * <p>
   * This method allows fine-grained control over the filter's configuration
   * through the CFReserveParams object, including bucket size, maximum iterations,
   * and expansion settings.
   * </p>
   *
   * @param key           the unique identifier for the filter
   * @param capacity      the expected number of items to be stored
   * @param reserveParams additional configuration parameters for the filter
   * @throws IllegalArgumentException if capacity is less than or equal to 0
   */
  void createFilter(String key, long capacity, CFReserveParams reserveParams);

  /**
   * Adds an item to the Cuckoo filter.
   * <p>
   * If the item already exists in the filter, this operation has no effect.
   * The method may return false if the filter is full and cannot accommodate
   * the new item after the maximum number of iteration attempts.
   * </p>
   *
   * @param key  the filter key
   * @param item the item to add
   * @return true if the item was successfully added, false otherwise
   */
  boolean add(String key, String item);

  /**
   * Adds an item to the Cuckoo filter only if it doesn't already exist.
   * <p>
   * This method provides conditional insertion semantics, adding the item
   * only if it's not already present in the filter. This can be useful
   * when you want to ensure uniqueness without performing separate
   * existence checks.
   * </p>
   *
   * @param key  the filter key
   * @param item the item to add conditionally
   * @return true if the item was added (didn't exist before), false if it already existed
   */
  boolean addNx(String key, String item);

  /**
   * Inserts multiple items into the Cuckoo filter in a single operation.
   * <p>
   * This batch operation is more efficient than individual add operations
   * when working with multiple items. Each item's insertion result is
   * returned in the corresponding position of the result list.
   * </p>
   *
   * @param key   the filter key
   * @param items the items to insert
   * @return a list of boolean values indicating the success of each insertion
   */
  List<Boolean> insert(String key, String... items);

  /**
   * Inserts multiple items into the Cuckoo filter with custom parameters.
   * <p>
   * This method allows configuring insertion behavior through CFInsertParams,
   * such as creating the filter automatically if it doesn't exist, setting
   * capacity, and controlling error rates.
   * </p>
   *
   * @param key          the filter key
   * @param insertParams parameters controlling the insertion behavior
   * @param items        the items to insert
   * @return a list of boolean values indicating the success of each insertion
   */
  List<Boolean> insert(String key, CFInsertParams insertParams, String... items);

  /**
   * Inserts multiple items into the Cuckoo filter only if they don't already exist.
   * <p>
   * This batch operation combines the efficiency of bulk insertion with
   * conditional semantics, adding only items that are not already present
   * in the filter.
   * </p>
   *
   * @param key   the filter key
   * @param items the items to insert conditionally
   * @return a list of boolean values indicating which items were added (true) or already existed (false)
   */
  List<Boolean> insertNx(String key, String... items);

  /**
   * Inserts multiple items into the Cuckoo filter conditionally with custom parameters.
   * <p>
   * This method combines conditional insertion (only if items don't exist)
   * with configurable insertion parameters, providing maximum flexibility
   * for bulk operations.
   * </p>
   *
   * @param key          the filter key
   * @param insertParams parameters controlling the insertion behavior
   * @param items        the items to insert conditionally
   * @return a list of boolean values indicating which items were added (true) or already existed (false)
   */
  List<Boolean> insertNx(String key, CFInsertParams insertParams, String... items);

  /**
   * Tests whether an item exists in the Cuckoo filter.
   * <p>
   * This operation performs approximate membership testing. A result of true
   * indicates the item might be in the filter (with a small probability of
   * false positives), while false definitively means the item is not present
   * (no false negatives).
   * </p>
   *
   * @param key  the filter key
   * @param item the item to test for membership
   * @return true if the item might exist in the filter, false if it definitely doesn't
   */
  boolean exists(String key, String item);

  /**
   * Tests whether multiple items exist in the Cuckoo filter.
   * <p>
   * This batch operation efficiently checks membership for multiple items
   * in a single call. Each result follows the same semantics as the single-item
   * exists method.
   * </p>
   *
   * @param key   the filter key
   * @param items the items to test for membership
   * @return a list of boolean values indicating potential membership for each item
   */
  List<Boolean> exists(String key, String... items);

  /**
   * Deletes an item from the Cuckoo filter.
   * <p>
   * This operation removes the specified item from the filter if it exists.
   * Unlike Bloom filters, Cuckoo filters support item deletion without
   * affecting the accuracy of other items in the filter.
   * </p>
   *
   * @param key  the filter key
   * @param item the item to delete
   * @return true if the item was successfully deleted, false if it wasn't found
   */
  boolean delete(String key, String item);

  /**
   * Returns the count of how many times an item appears in the Cuckoo filter.
   * <p>
   * This method provides frequency information for items in the filter.
   * The count represents how many times the item has been added (accounting
   * for any deletions that may have occurred).
   * </p>
   *
   * @param key  the filter key
   * @param item the item to count
   * @return the number of times the item appears in the filter (0 if not present)
   */
  long count(String key, String item);

  /**
   * Performs an incremental dump scan of the Cuckoo filter.
   * <p>
   * This method is used for serialization and backup purposes, allowing
   * the filter's internal state to be exported in chunks. The iterator
   * parameter is used to continue scanning from a previous position.
   * </p>
   *
   * @param key      the filter key
   * @param iterator the scan cursor (0 to start, or value from previous scan)
   * @return a map entry containing the next iterator position and chunk data
   */
  Map.Entry<Long, byte[]> scanDump(String key, long iterator);

  /**
   * Loads a chunk of data into the Cuckoo filter during restoration.
   * <p>
   * This method is used in conjunction with scanDump to restore a filter
   * from previously exported data. The iterator and data parameters must
   * correspond to values obtained from scanDump operations.
   * </p>
   *
   * @param key      the filter key
   * @param iterator the chunk position indicator
   * @param data     the chunk data to load
   * @return a status string indicating the success or failure of the operation
   */
  String loadChunk(String key, long iterator, byte[] data);

  /**
   * Returns information about the Cuckoo filter.
   * <p>
   * This method provides metadata about the filter's current state,
   * including statistics such as size, number of items, bucket count,
   * and other configuration details useful for monitoring and debugging.
   * </p>
   *
   * @param key the filter key
   * @return a map containing various statistics and configuration details about the filter
   */
  Map<String, Object> info(String key);
}
