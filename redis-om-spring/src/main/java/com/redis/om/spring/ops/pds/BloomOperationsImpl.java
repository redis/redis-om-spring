package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;

import com.redis.om.spring.client.RedisModulesClient;

import redis.clients.jedis.bloom.BFInsertParams;

public class BloomOperationsImpl<K> implements BloomOperations<K> {
  final RedisModulesClient client;

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
