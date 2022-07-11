package com.redis.om.spring.search.stream.actions;

import com.redis.om.spring.ops.json.JSONOperations;

public interface TakesJSONOperations {
  void setJSONOperations(JSONOperations<String> json);
}
