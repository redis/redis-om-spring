package com.redislabs.spring.ops.pds;

import com.redislabs.spring.client.RedisModulesClient;

public class CuckooFilterOperationsImpl<K> implements CuckooFilterOperations<K> {
  RedisModulesClient client;

  public CuckooFilterOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }
}
