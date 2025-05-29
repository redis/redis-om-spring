package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.bloom.TDigestMergeParams;

/**
 * Interface for T-Digest probabilistic data structure operations in Redis.
 * 
 * <p>T-Digest is a probabilistic data structure for estimating quantiles and percentiles
 * from streaming data or large datasets. It provides accurate estimates while using
 * minimal memory, making it ideal for:</p>
 * <ul>
 * <li>Real-time analytics and monitoring</li>
 * <li>Computing percentiles (e.g., P50, P95, P99) for latency measurements</li>
 * <li>Estimating cumulative distribution functions</li>
 * <li>Finding median values in large datasets</li>
 * <li>Anomaly detection based on statistical distributions</li>
 * </ul>
 * 
 * <p>Key features of T-Digest:</p>
 * <ul>
 * <li>Constant memory usage regardless of data size</li>
 * <li>High accuracy for extreme quantiles (near 0 or 1)</li>
 * <li>Support for merging multiple digests</li>
 * <li>Fast updates and queries</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * TDigestOperations<String> ops = redisModulesOperations.opsForTDigest();
 * 
 * // Create a new T-Digest for response times
 * ops.create("response_times", 100);
 * 
 * // Add measurements
 * ops.add("response_times", 15.2, 18.5, 22.1, 14.8, 25.3);
 * 
 * // Get percentiles
 * List<Double> percentiles = ops.quantile("response_times", 0.5, 0.95, 0.99);
 * // Returns [18.5, 24.7, 25.2] (P50, P95, P99)
 * 
 * // Get cumulative distribution
 * List<Double> cdf = ops.cdf("response_times", 20.0);
 * // Returns [0.75] (75% of values are <= 20.0)
 * }</pre>
 *
 * @param <K> the Redis key type
 * @author Redis OM Spring Developers
 * @see BloomOperations
 * @see CountMinSketchOperations
 * @see CuckooFilterOperations
 */
public interface TDigestOperations<K> {
  /**
   * Create a new T-Digest sketch.
   *
   * @param key The key of the sketch
   * @return Status string reply
   */
  String create(K key);

  /**
   * Create a new T-Digest sketch with specified compression.
   *
   * @param key         The key of the sketch
   * @param compression The compression parameter
   * @return Status string reply
   */
  String create(K key, int compression);

  /**
   * Reset the sketch.
   *
   * @param key The key of the sketch
   * @return Status string reply
   */
  String reset(K key);

  /**
   * Merge multiple sketches into one.
   *
   * @param key        The key of the destination sketch
   * @param sourceKeys The keys of the source sketches
   * @return Status string reply
   */
  String merge(K key, K... sourceKeys);

  /**
   * Merge multiple sketches into one with parameters.
   *
   * @param params     The merge parameters
   * @param key        The key of the destination sketch
   * @param sourceKeys The keys of the source sketches
   * @return Status string reply
   */
  String merge(TDigestMergeParams params, K key, K... sourceKeys);

  /**
   * Get information about the sketch.
   *
   * @param key The key of the sketch
   * @return Map of information about the sketch
   */
  Map<String, Object> info(K key);

  /**
   * Add values to the sketch.
   *
   * @param key    The key of the sketch
   * @param values The values to add
   * @return Status string reply
   */
  String add(K key, double... values);

  /**
   * Get the CDF (Cumulative Distribution Function) for values.
   *
   * @param key    The key of the sketch
   * @param values The values to get CDF for
   * @return List of CDF values
   */
  List<Double> cdf(K key, double... values);

  /**
   * Get the quantile for values.
   *
   * @param key    The key of the sketch
   * @param values The values to get quantile for
   * @return List of quantile values
   */
  List<Double> quantile(K key, double... values);

  /**
   * Get the minimum value in the sketch.
   *
   * @param key The key of the sketch
   * @return The minimum value
   */
  double min(K key);

  /**
   * Get the maximum value in the sketch.
   *
   * @param key The key of the sketch
   * @return The maximum value
   */
  double max(K key);

  /**
   * Get the trimmed mean of the sketch.
   *
   * @param key     The key of the sketch
   * @param lowCut  The low cut quantile
   * @param highCut The high cut quantile
   * @return The trimmed mean
   */
  double trimmedMean(K key, double lowCut, double highCut);

  /**
   * Get the rank of values.
   *
   * @param key    The key of the sketch
   * @param values The values to get rank for
   * @return List of ranks
   */
  List<Long> rank(K key, double... values);

  /**
   * Get the reverse rank of values.
   *
   * @param key    The key of the sketch
   * @param values The values to get reverse rank for
   * @return List of reverse ranks
   */
  List<Long> revRank(K key, double... values);

  /**
   * Get the value by rank.
   *
   * @param key   The key of the sketch
   * @param ranks The ranks to get values for
   * @return List of values
   */
  List<Double> byRank(K key, long... ranks);

  /**
   * Get the value by reverse rank.
   *
   * @param key   The key of the sketch
   * @param ranks The reverse ranks to get values for
   * @return List of values
   */
  List<Double> byRevRank(K key, long... ranks);
}