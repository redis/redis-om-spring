package com.redis.spring.ops.pds;

import com.redis.spring.client.RedisModulesClient;

public class CuckooFilterOperationsImpl<K> implements CuckooFilterOperations<K> {
  RedisModulesClient client;

  public CuckooFilterOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }
}
