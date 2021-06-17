package com.redislabs.spring.ops.pds;

import com.redislabs.spring.client.RedisModulesClient;

public class BloomOperationsImpl<K> implements BloomOperations<K> {
  RedisModulesClient client;

  public BloomOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }
}
