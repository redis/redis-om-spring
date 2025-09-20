package com.redis.om.spring.indexing;

/**
 * Strategy interface for resolving index names and key prefixes.
 * Implementations can provide custom logic for determining index
 * configurations based on runtime context.
 *
 * <p>This interface allows for dynamic index resolution based on
 * factors such as tenant ID, environment, or custom attributes
 * provided through {@link RedisIndexContext}.
 *
 * @since 1.0.0
 */
public interface IndexResolver {

  /**
   * Resolves the index name for a given entity class.
   *
   * @param entityClass the entity class to resolve the index for
   * @param context     the current index context, may be null
   * @return the resolved index name
   */
  String resolveIndexName(Class<?> entityClass, RedisIndexContext context);

  /**
   * Resolves the key prefix for a given entity class.
   *
   * @param entityClass the entity class to resolve the prefix for
   * @param context     the current index context, may be null
   * @return the resolved key prefix
   */
  String resolveKeyPrefix(Class<?> entityClass, RedisIndexContext context);
}