package com.redis.om.spring.search.stream;

import static com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively;

import java.lang.reflect.Field;
import java.util.Optional;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;

/**
 * Implementation of EntityStream for creating search streams.
 */
public class EntityStreamImpl implements EntityStream {

  private final RedisModulesOperations<String> modulesOperations;
  private final GsonBuilder gsonBuilder;

  private final RediSearchIndexer indexer;

  /**
   * Creates a new EntityStreamImpl.
   *
   * @param rmo         the Redis modules operations
   * @param gsonBuilder the Gson builder
   * @param indexer     the Redis search indexer
   */
  @SuppressWarnings(
    "unchecked"
  )
  public EntityStreamImpl(RedisModulesOperations<?> rmo, GsonBuilder gsonBuilder, RediSearchIndexer indexer) {
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.gsonBuilder = gsonBuilder;
    this.indexer = indexer;
  }

  @Override
  public <E> SearchStream<E> of(Class<E> entityClass) {
    return new SearchStreamImpl<>(entityClass, modulesOperations, gsonBuilder, indexer);
  }

  @Override
  public <E> SearchStream<E> of(Class<E> entityClass, String searchIndex, String idField) {
    Optional<Field> maybeIdField = getDeclaredFieldsTransitively(entityClass).stream().filter(f -> f.getName().equals(
        "id")).findFirst();
    if (maybeIdField.isPresent()) {
      return new SearchStreamImpl<>(entityClass, searchIndex, maybeIdField.get(), modulesOperations, gsonBuilder,
          indexer);
    } else {
      throw new IllegalArgumentException(entityClass.getName() + " does not appear to have an ID field");
    }

  }

}
