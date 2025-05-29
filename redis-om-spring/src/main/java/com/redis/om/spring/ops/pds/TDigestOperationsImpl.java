package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

import com.redis.om.spring.client.RedisModulesClient;

import redis.clients.jedis.bloom.TDigestMergeParams;

/**
 * Implementation of T-Digest operations for Redis.
 * 
 * <p>This class provides the concrete implementation of T-Digest probabilistic
 * data structure operations using the Redis Modules Client. T-Digest is used
 * for accurate quantile estimation in streaming data scenarios.</p>
 * 
 * <p>Thread-safety: This implementation is thread-safe as it delegates all
 * operations to the underlying Redis client.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * RedisModulesClient client = new RedisModulesClient(jedisPool);
 * TDigestOperations<String> tdigest = new TDigestOperationsImpl<>(client);
 * 
 * // Track API response times
 * tdigest.create("api_response_times", 500);
 * tdigest.add("api_response_times", 120.5, 145.2, 98.7, 234.1);
 * 
 * // Get 95th percentile
 * double p95 = tdigest.quantile("api_response_times", 0.95).get(0);
 * }</pre>
 *
 * @param <K> the Redis key type
 * @author Redis OM Spring Developers
 * @see TDigestOperations
 * @see RedisModulesClient
 */
public class TDigestOperationsImpl<K> implements TDigestOperations<K> {
  /**
   * The Redis Modules client used for executing T-Digest commands.
   */
  final RedisModulesClient client;

  /**
   * Constructs a new TDigestOperationsImpl with the specified Redis client.
   * 
   * @param client the Redis Modules client to use for operations
   * @throws IllegalArgumentException if client is null
   */
  public TDigestOperationsImpl(RedisModulesClient client) {
    if (client == null) {
      throw new IllegalArgumentException("RedisModulesClient cannot be null");
    }
    this.client = client;
  }

  @Override
  public String create(K key) {
    return client.clientForTDigest().tdigestCreate(key.toString());
  }

  @Override
  public String create(K key, int compression) {
    return client.clientForTDigest().tdigestCreate(key.toString(), compression);
  }

  @Override
  public String reset(K key) {
    return client.clientForTDigest().tdigestReset(key.toString());
  }

  @Override
  public String merge(K key, K... sourceKeys) {
    String[] sourceKeyStrings = new String[sourceKeys.length];
    for (int i = 0; i < sourceKeys.length; i++) {
      sourceKeyStrings[i] = sourceKeys[i].toString();
    }
    return client.clientForTDigest().tdigestMerge(key.toString(), sourceKeyStrings);
  }

  @Override
  public String merge(TDigestMergeParams params, K key, K... sourceKeys) {
    String[] sourceKeyStrings = new String[sourceKeys.length];
    for (int i = 0; i < sourceKeys.length; i++) {
      sourceKeyStrings[i] = sourceKeys[i].toString();
    }
    return client.clientForTDigest().tdigestMerge(params, key.toString(), sourceKeyStrings);
  }

  @Override
  public Map<String, Object> info(K key) {
    return client.clientForTDigest().tdigestInfo(key.toString());
  }

  @Override
  public String add(K key, double... values) {
    return client.clientForTDigest().tdigestAdd(key.toString(), values);
  }

  @Override
  public List<Double> cdf(K key, double... values) {
    return client.clientForTDigest().tdigestCDF(key.toString(), values);
  }

  @Override
  public List<Double> quantile(K key, double... values) {
    return client.clientForTDigest().tdigestQuantile(key.toString(), values);
  }

  @Override
  public double min(K key) {
    return client.clientForTDigest().tdigestMin(key.toString());
  }

  @Override
  public double max(K key) {
    return client.clientForTDigest().tdigestMax(key.toString());
  }

  @Override
  public double trimmedMean(K key, double lowCut, double highCut) {
    return client.clientForTDigest().tdigestTrimmedMean(key.toString(), lowCut, highCut);
  }

  @Override
  public List<Long> rank(K key, double... values) {
    return client.clientForTDigest().tdigestRank(key.toString(), values);
  }

  @Override
  public List<Long> revRank(K key, double... values) {
    return client.clientForTDigest().tdigestRevRank(key.toString(), values);
  }

  @Override
  public List<Double> byRank(K key, long... ranks) {
    return client.clientForTDigest().tdigestByRank(key.toString(), ranks);
  }

  @Override
  public List<Double> byRevRank(K key, long... ranks) {
    return client.clientForTDigest().tdigestByRevRank(key.toString(), ranks);
  }
}