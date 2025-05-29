package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

import com.redis.om.spring.client.RedisModulesClient;

import redis.clients.jedis.bloom.BFInsertParams;

/**
 * Default implementation of {@link BloomOperations} that delegates to Redis Bloom Filter commands.
 * <p>
 * This implementation provides concrete Redis Bloom Filter operations by wrapping the underlying
 * Jedis Bloom Filter client. It handles the conversion of operation parameters and manages the
 * communication with Redis Stack's Bloom Filter module.
 * </p>
 * <p>
 * The implementation is automatically configured by Redis OM Spring when Bloom Filter
 * functionality is enabled and is used by the {@link com.redis.om.spring.bloom.BloomAspect}
 * for transparent Bloom Filter maintenance during entity operations.
 * </p>
 *
 * @param <K> the type of keys used to identify bloom filters
 * @see BloomOperations
 * @see com.redis.om.spring.client.RedisModulesClient
 * @since 0.1.0
 */
public class BloomOperationsImpl<K> implements BloomOperations<K> {
  final RedisModulesClient client;

  /**
   * Creates a new BloomOperationsImpl with the specified Redis modules client.
   *
   * @param client the Redis modules client for executing Bloom Filter commands
   */
  public BloomOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public void createFilter(K name, long initCapacity, double errorRate) {
    client.clientForBloom().bfReserve(name.toString(), errorRate, initCapacity);
  }

  @Override
  public boolean add(K name, String value) {
    return client.clientForBloom().bfAdd(name.toString(), value);
  }

  @Override
  public List<Boolean> insert(K name, BFInsertParams options, String... items) {
    return client.clientForBloom().bfInsert(name.toString(), options, items);
  }

  @Override
  public List<Boolean> addMulti(K name, String... values) {
    return client.clientForBloom().bfMAdd(name.toString(), values);
  }

  @Override
  public boolean exists(K name, String value) {
    return client.clientForBloom().bfExists(name.toString(), value);
  }

  @Override
  public List<Boolean> existsMulti(K name, String... values) {
    return client.clientForBloom().bfMExists(name.toString(), values);
  }

  @Override
  public Map<String, Object> info(K name) {
    return client.clientForBloom().bfInfo(name.toString());
  }

}
