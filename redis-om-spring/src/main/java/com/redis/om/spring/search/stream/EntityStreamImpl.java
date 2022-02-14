package com.redis.om.spring.search.stream;

import com.redis.om.spring.ops.RedisModulesOperations;

public class EntityStreamImpl implements EntityStream {

  RedisModulesOperations<String, String> modulesOperations;

  @SuppressWarnings("unchecked")
  public EntityStreamImpl(RedisModulesOperations<?, ?> rmo) {
    this.modulesOperations = (RedisModulesOperations<String, String>) rmo;
  }

  @Override
  public <E> SearchStream<E> of(Class<E> entityClass) {
    return new SearchStreamImpl<E>(entityClass, modulesOperations);
  }

}
