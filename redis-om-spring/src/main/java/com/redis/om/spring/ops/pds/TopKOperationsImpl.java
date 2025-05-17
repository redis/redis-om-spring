package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

import com.redis.om.spring.client.RedisModulesClient;

public class TopKOperationsImpl<K> implements TopKOperations<K> {
  final RedisModulesClient client;

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
