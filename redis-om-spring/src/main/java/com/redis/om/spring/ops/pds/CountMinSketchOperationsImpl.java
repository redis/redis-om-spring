package com.redis.om.spring.ops.pds;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.redis.om.spring.client.RedisModulesClient;

public class CountMinSketchOperationsImpl<K> implements CountMinSketchOperations<K> {
  RedisModulesClient client;

  public CountMinSketchOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public void cmsInitByDim(K key, long width, long depth) {
    client.clientForBloom().cmsInitByDim(key.toString(), width, depth);
  }

  @Override
  public void cmsInitByProb(K key, double error, double probability) {
    client.clientForBloom().cmsInitByProb(key.toString(), error, probability);
  }

  @Override
  public long cmsIncrBy(K key, String item, long increment) {
    return client.clientForBloom().cmsIncrBy(key.toString(), item, increment);
  }

  @Override
  public List<Long> cmsIncrBy(K key, Map<String, Long> itemIncrements) {
    return client.clientForBloom().cmsIncrBy(key.toString(), itemIncrements);
  }

  @Override
  public List<Long> cmsQuery(K key, String... items) {
    return client.clientForBloom().cmsQuery(key.toString(), items);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void cmsMerge(K destKey, K... keys) {
    client.clientForBloom().cmsMerge( //
        destKey.toString(), //
        Arrays.asList(keys).stream().map(Object::toString).toArray(String[]::new));
  }

  @Override
  public void cmsMerge(K destKey, Map<K, Long> keysAndWeights) {
    client.clientForBloom().cmsMerge( //
        destKey.toString(), //
        keysAndWeights //
            .entrySet() //
            .stream() //
            .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
  }

  @Override
  public Map<String, Long> cmsInfo(K key) {
    return client.clientForBloom().cmsInfo(key.toString());
  }
}

