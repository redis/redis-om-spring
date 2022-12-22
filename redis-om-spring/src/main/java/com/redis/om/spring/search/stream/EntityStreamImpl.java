package com.redis.om.spring.search.stream;

import com.google.gson.Gson;
import com.redis.om.spring.ops.RedisModulesOperations;

public class EntityStreamImpl implements EntityStream {

  final RedisModulesOperations<String> modulesOperations;
  final Gson gson;

  @SuppressWarnings("unchecked")
  public EntityStreamImpl(RedisModulesOperations<?> rmo, Gson gson) {
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.gson = gson;
  }

  @Override
  public <E> SearchStream<E> of(Class<E> entityClass) {
    return new SearchStreamImpl<>(entityClass, modulesOperations, gson);
  }

}
