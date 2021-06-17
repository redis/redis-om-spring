package com.redislabs.spring.ops;

import com.redislabs.spring.client.RedisModulesClient;
import com.redislabs.spring.ops.ai.AIOperations;
import com.redislabs.spring.ops.ai.AIOperationsImpl;
import com.redislabs.spring.ops.graph.GraphOperations;
import com.redislabs.spring.ops.graph.GraphOperationsImpl;
import com.redislabs.spring.ops.json.JSONOperations;
import com.redislabs.spring.ops.json.JSONOperationsImpl;
import com.redislabs.spring.ops.pds.BloomOperations;
import com.redislabs.spring.ops.pds.BloomOperationsImpl;
import com.redislabs.spring.ops.pds.CountMinSketchOperations;
import com.redislabs.spring.ops.pds.CountMinSketchOperationsImpl;
import com.redislabs.spring.ops.pds.CuckooFilterOperations;
import com.redislabs.spring.ops.pds.CuckooFilterOperationsImpl;
import com.redislabs.spring.ops.pds.TopKOperations;
import com.redislabs.spring.ops.pds.TopKOperationsImpl;
import com.redislabs.spring.ops.search.SearchOperations;
import com.redislabs.spring.ops.search.SearchOperationsImpl;
import com.redislabs.spring.ops.timeseries.TimeSeriesOperations;
import com.redislabs.spring.ops.timeseries.TimeSeriesOperationsImpl;

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
