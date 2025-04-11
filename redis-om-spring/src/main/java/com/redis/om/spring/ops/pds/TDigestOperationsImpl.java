package com.redis.om.spring.ops.pds;

import com.redis.om.spring.client.RedisModulesClient;
import redis.clients.jedis.bloom.TDigestMergeParams;

import java.util.List;
import java.util.Map;

public class TDigestOperationsImpl<K> implements TDigestOperations<K> {
  final RedisModulesClient client;

  public TDigestOperationsImpl(RedisModulesClient client) {
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