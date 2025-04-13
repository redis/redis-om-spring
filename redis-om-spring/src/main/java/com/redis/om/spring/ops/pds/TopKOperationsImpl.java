package com.redis.om.spring.ops.pds;

import com.redis.om.spring.client.RedisModulesClient;

public class TopKOperationsImpl<K> implements TopKOperations<K> {
  final RedisModulesClient client;

  public TopKOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }
}
