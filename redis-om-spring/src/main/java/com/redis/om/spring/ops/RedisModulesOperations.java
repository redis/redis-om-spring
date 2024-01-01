package com.redis.om.spring.ops;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.json.JSONOperationsImpl;
import com.redis.om.spring.ops.pds.*;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.ops.search.SearchOperationsImpl;
import org.springframework.data.redis.core.StringRedisTemplate;

public record RedisModulesOperations<K>(RedisModulesClient client, StringRedisTemplate template, GsonBuilder gsonBuilder) {

  public JSONOperations<K> opsForJSON() {
    return new JSONOperationsImpl<>(client, gsonBuilder);
  }

  public SearchOperations<K> opsForSearch(K index) {
    return new SearchOperationsImpl<>(index, client, template);
  }

  public BloomOperations<K> opsForBloom() {
    return new BloomOperationsImpl<>(client);
  }

  public CountMinSketchOperations<K> opsForCountMinSketch() {
    return new CountMinSketchOperationsImpl<>(client);
  }

  public CuckooFilterOperations<K> opsForCuckoFilter() {
    return new CuckooFilterOperationsImpl<>(client);
  }

  public TopKOperations<K> opsForTopK() {
    return new TopKOperationsImpl<>(client);
  }
}
