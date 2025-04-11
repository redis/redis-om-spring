package com.redis.om.spring.ops.pds;

import redis.clients.jedis.bloom.TDigestMergeParams;

import java.util.List;
import java.util.Map;

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
   * @param key       The key of the destination sketch
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