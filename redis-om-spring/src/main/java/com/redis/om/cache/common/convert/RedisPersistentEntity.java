package com.redis.om.cache.common.convert;

import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.mapping.PersistentEntity;

/**
 * Redis specific {@link PersistentEntity} that represents a persistent entity stored in Redis.
 * This interface extends the Spring Data {@link KeyValuePersistentEntity} with Redis-specific
 * functionality for managing entity metadata and mapping between domain objects and Redis data structures.
 *
 * @param <T> the type of the persistent entity
 */
public interface RedisPersistentEntity<T> extends KeyValuePersistentEntity<T, RedisPersistentProperty> {

}
