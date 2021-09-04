package com.redis.spring.ops.pds;

import com.redis.spring.client.RedisModulesClient;

public class TopKOperationsImpl<K> implements TopKOperations<K> {
  RedisModulesClient client;

  public TopKOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }
}
