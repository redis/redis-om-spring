package com.redis.om.spring.search.stream;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.ops.RedisModulesOperations;

public class EntityStreamImpl implements EntityStream {

  private final RedisModulesOperations<String> modulesOperations;
  private final GsonBuilder gsonBuilder;

  @SuppressWarnings("unchecked")
  public EntityStreamImpl(RedisModulesOperations<?> rmo, GsonBuilder gsonBuilder) {
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.gsonBuilder = gsonBuilder;
  }

  @Override
  public <E> SearchStream<E> of(Class<E> entityClass) {
    return new SearchStreamImpl<>(entityClass, modulesOperations, gsonBuilder);
  }

}
