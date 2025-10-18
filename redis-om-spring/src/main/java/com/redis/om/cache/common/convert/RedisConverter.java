package com.redis.om.cache.common.convert;

import org.springframework.data.convert.EntityConverter;
import org.springframework.data.mapping.model.EntityInstantiators;

/**
 * Redis specific {@link EntityConverter} that handles conversion between domain objects and
 * Redis data structures. This interface extends the Spring Data {@link EntityConverter} with
 * Redis-specific functionality.
 */
public interface RedisConverter extends
    EntityConverter<RedisPersistentEntity<?>, RedisPersistentProperty, Object, RedisData> {

  /**
   * Returns the mapping context used by this converter.
   * 
   * @return the {@link RedisMappingContext} used by this converter
   */
  @Override
  RedisMappingContext getMappingContext();

  /**
   * Returns the entity instantiators used by this converter.
   * 
   * @return the configured {@link EntityInstantiators}
   * @since 3.2.4
   */
  EntityInstantiators getEntityInstantiators();
}
