package com.redis.om.spring.search.stream;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import org.springframework.expression.spel.ast.Indexer;

public class EntityStreamImpl implements EntityStream {

  private final RedisModulesOperations<String> modulesOperations;
  private final GsonBuilder gsonBuilder;

  private final RediSearchIndexer indexer;

  @SuppressWarnings("unchecked")
  public EntityStreamImpl(RedisModulesOperations<?> rmo, GsonBuilder gsonBuilder, RediSearchIndexer indexer) {
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.gsonBuilder = gsonBuilder;
    this.indexer = indexer;
  }

  @Override
  public <E> SearchStream<E> of(Class<E> entityClass) {
    return new SearchStreamImpl<>(entityClass, modulesOperations, gsonBuilder, indexer);
  }

}
