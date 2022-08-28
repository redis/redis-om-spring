package com.redis.om.spring.ops;

import org.springframework.data.redis.core.RedisTemplate;

import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.json.JSONOperationsImpl;
import com.redis.om.spring.ops.pds.BloomOperations;
import com.redis.om.spring.ops.pds.BloomOperationsImpl;
import com.redis.om.spring.ops.pds.CountMinSketchOperations;
import com.redis.om.spring.ops.pds.CountMinSketchOperationsImpl;
import com.redis.om.spring.ops.pds.CuckooFilterOperations;
import com.redis.om.spring.ops.pds.CuckooFilterOperationsImpl;
import com.redis.om.spring.ops.pds.TopKOperations;
import com.redis.om.spring.ops.pds.TopKOperationsImpl;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.ops.search.SearchOperationsImpl;

public class RedisModulesOperations<K> {

  private RedisModulesClient client;
  private RedisTemplate<?, ?> template;

  public RedisModulesOperations(RedisModulesClient client, RedisTemplate<?, ?> template) {
    this.client = client;
    this.template = template;
  }

  public JSONOperations<K> opsForJSON() {
    return new JSONOperationsImpl<>(client);
  }

  public SearchOperations<K> opsForSearch(K index) {
    return new SearchOperationsImpl<>(index, client);
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

  public RedisTemplate<?, ?> getTemplate() {
    return template;
  }

  public RedisModulesClient getClient() {
    return client;
  }
}
