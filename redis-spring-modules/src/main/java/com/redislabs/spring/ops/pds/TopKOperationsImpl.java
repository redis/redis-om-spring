package com.redislabs.spring.ops.pds;

import com.redislabs.spring.client.RedisModulesClient;

public class TopKOperationsImpl<K> implements TopKOperations<K> {
  RedisModulesClient client;

  public TopKOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }
}
