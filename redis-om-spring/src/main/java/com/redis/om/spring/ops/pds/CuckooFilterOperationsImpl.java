package com.redis.om.spring.ops.pds;

import com.redis.om.spring.client.RedisModulesClient;

public class CuckooFilterOperationsImpl<K> implements CuckooFilterOperations<K> {
  RedisModulesClient client;

  public CuckooFilterOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }
}
