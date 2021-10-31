package com.redis.om.spring.ops;

import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.ai.AIOperations;
import com.redis.om.spring.ops.ai.AIOperationsImpl;
import com.redis.om.spring.ops.graph.GraphOperations;
import com.redis.om.spring.ops.graph.GraphOperationsImpl;
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
import com.redis.om.spring.ops.timeseries.TimeSeriesOperations;
import com.redis.om.spring.ops.timeseries.TimeSeriesOperationsImpl;

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
