package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

import com.redis.om.spring.client.RedisModulesClient;

/**
 * Implementation of TopK operations for Redis TopK probabilistic data structure.
 * 
 * <p>This class provides concrete implementations for all TopK operations defined
 * in the {@link TopKOperations} interface. It delegates the actual Redis commands
 * to the underlying {@link RedisModulesClient}.
 * 
 * <p>The TopK data structure is particularly useful for:
 * <ul>
 * <li>Real-time analytics where tracking exact counts is impractical</li>
 * <li>Memory-constrained environments that need frequency tracking</li>
 * <li>High-throughput systems that need to identify popular items</li>
 * <li>Streaming applications that process continuous data flows</li>
 * </ul>
 * 
 * <p>Thread-safety: This implementation is thread-safe if the underlying
 * {@link RedisModulesClient} is thread-safe.
 * 
 * @param <K> The type of keys used to identify TopK filters
 * @see TopKOperations
 * @see RedisModulesClient
 */
public class TopKOperationsImpl<K> implements TopKOperations<K> {
  /**
   * The Redis modules client used to execute TopK commands.
   */
  final RedisModulesClient client;

  /**
   * Constructs a new TopKOperationsImpl with the specified Redis modules client.
   * 
   * @param client The Redis modules client for executing commands
   */
  public TopKOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public String createFilter(K key, long topk) {
    return client.clientForTopK().topkReserve(key.toString(), topk);
  }

  @Override
  public String createFilter(K key, long topk, long width, long depth, double decay) {
    return client.clientForTopK().topkReserve(key.toString(), topk, width, depth, decay);
  }

  @Override
  public List<String> add(K key, String... items) {
    return client.clientForTopK().topkAdd(key.toString(), items);
  }

  @Override
  public String incrementBy(K key, String item, long increment) {
    return client.clientForTopK().topkIncrBy(key.toString(), item, increment);
  }

  @Override
  public List<String> incrementBy(K key, Map<String, Long> itemIncrementMap) {
    return client.clientForTopK().topkIncrBy(key.toString(), itemIncrementMap);
  }

  @Override
  public List<Boolean> query(K key, String... items) {
    return client.clientForTopK().topkQuery(key.toString(), items);
  }

  @Override
  public List<String> list(K key) {
    return client.clientForTopK().topkList(key.toString());
  }

  @Override
  public Map<String, Long> listWithCount(K key) {
    return client.clientForTopK().topkListWithCount(key.toString());
  }

  @Override
  public Map<String, Object> info(K key) {
    return client.clientForTopK().topkInfo(key.toString());
  }
}
