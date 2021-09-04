package com.redis.spring.ops;

import com.redis.spring.client.RedisModulesClient;
import com.redis.spring.ops.ai.AIOperations;
import com.redis.spring.ops.ai.AIOperationsImpl;
import com.redis.spring.ops.graph.GraphOperations;
import com.redis.spring.ops.graph.GraphOperationsImpl;
import com.redis.spring.ops.json.JSONOperations;
import com.redis.spring.ops.json.JSONOperationsImpl;
import com.redis.spring.ops.pds.BloomOperations;
import com.redis.spring.ops.pds.BloomOperationsImpl;
import com.redis.spring.ops.pds.CountMinSketchOperations;
import com.redis.spring.ops.pds.CountMinSketchOperationsImpl;
import com.redis.spring.ops.pds.CuckooFilterOperations;
import com.redis.spring.ops.pds.CuckooFilterOperationsImpl;
import com.redis.spring.ops.pds.TopKOperations;
import com.redis.spring.ops.pds.TopKOperationsImpl;
import com.redis.spring.ops.search.SearchOperations;
import com.redis.spring.ops.search.SearchOperationsImpl;
import com.redis.spring.ops.timeseries.TimeSeriesOperations;
import com.redis.spring.ops.timeseries.TimeSeriesOperationsImpl;

public class RedisModulesOperations<K,V> {
  
  private RedisModulesClient client;
  
  public RedisModulesOperations(RedisModulesClient client) {
    this.client = client;
  }

  public JSONOperations<K> opsForJSON() {
    return new JSONOperationsImpl<>(client);
  }
  
  public GraphOperations<K> opsForGraph() {
    return new GraphOperationsImpl<>(client);
  }
  
  public SearchOperations<K> opsForSearch(K index) {
    return new SearchOperationsImpl<>(index, client);
  }
  
  public TimeSeriesOperations<K> opsForTimeSeries() {
    return new TimeSeriesOperationsImpl<>(client);
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
  
  public AIOperations<K> opsForAI() {
    return new AIOperationsImpl<>(client);
  }

}
