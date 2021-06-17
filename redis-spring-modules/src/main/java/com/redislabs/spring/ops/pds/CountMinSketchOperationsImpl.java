package com.redislabs.spring.ops.pds;

import com.redislabs.spring.client.RedisModulesClient;

public class CountMinSketchOperationsImpl<K> implements CountMinSketchOperations<K> {
  RedisModulesClient client;

  public CountMinSketchOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }
}
