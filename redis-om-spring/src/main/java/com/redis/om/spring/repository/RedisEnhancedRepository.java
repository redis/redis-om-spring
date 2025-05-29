package com.redis.om.spring.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.redis.om.spring.metamodel.MetamodelField;

/**
 * Enhanced Redis repository interface that provides advanced functionality for entities
 * stored as Redis hash structures with RediSearch indexing capabilities.
 * <p>
 * This repository interface extends {@link KeyValueRepository} and {@link QueryByExampleExecutor}
 * to provide Redis-specific operations including field-level updates, expiration management,
 * and efficient querying capabilities through RediSearch.
 * <p>
 * Unlike {@link RedisDocumentRepository} which stores entities as JSON documents,
 * RedisEnhancedRepository stores entities as Redis hash structures, making them suitable
 * for scenarios where individual field access and updates are frequently needed.
 * <p>
 * To enable this repository type, use {@code @EnableRedisEnhancedRepositories} on your
 * configuration class and annotate your entity classes with {@code @RedisHash}.
 *
 * @param <T>  the entity type managed by this repository
 * @param <ID> the type of the entity's identifier
 * @see KeyValueRepository
 * @see QueryByExampleExecutor
 * @see RedisDocumentRepository
 * @since 1.0
 */
@NoRepositoryBean
public interface RedisEnhancedRepository<T, ID> extends KeyValueRepository<T, ID>, QueryByExampleExecutor<T> {

  /**
   * Retrieves all entity identifiers from the repository.
   * <p>
   * This method provides an efficient way to obtain all IDs without loading
   * the complete entity data, which is useful for bulk operations or when
   * only identifiers are needed.
   *
   * @return an {@link Iterable} containing all entity identifiers
   */
  Iterable<ID> getIds();

  /**
   * Returns a {@link Page} of ids meeting the paging restriction provided in
   * the {@code Pageable} object.
   *
   * @param pageable encapsulates pagination information
   * @return a page of ids
   */
  Page<ID> getIds(Pageable pageable);

  /**
   * Updates a specific field of an entity without loading or modifying other fields.
   * <p>
   * This method provides efficient field-level updates by directly modifying
   * the hash field in Redis, avoiding the need to load the entire entity,
   * modify it, and save it back.
   *
   * @param entity the entity whose field should be updated
   * @param field  the metamodel field specification for the field to update
   * @param value  the new value to set for the field
   * @throws IllegalArgumentException if the entity, field, or value is invalid
   */
  void updateField(T entity, MetamodelField<T, ?> field, Object value);

  /**
   * Retrieves specific field values for multiple entities by their identifiers.
   * <p>
   * This method provides efficient bulk field retrieval without loading complete
   * entities, which is particularly useful when only specific field values are
   * needed for a large number of entities.
   *
   * @param ids   the identifiers of the entities whose field values to retrieve
   * @param field the metamodel field specification for the field to retrieve
   * @param <F>   the type of the field being retrieved
   * @return an {@link Iterable} containing the field values in the same order as the provided IDs
   * @throws IllegalArgumentException if the field specification is invalid
   */
  <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field);

  /**
   * Retrieves the expiration time (TTL) for an entity in seconds.
   * <p>
   * Returns the remaining time to live for the entity with the specified ID.
   * If the entity has no expiration set, this method returns -1. If the entity
   * does not exist, this method returns -2.
   *
   * @param id the identifier of the entity
   * @return the expiration time in seconds, -1 if no expiration is set, -2 if entity doesn't exist
   * @throws IllegalArgumentException if the ID is null
   */
  Long getExpiration(ID id);

  /**
   * Sets an expiration time (TTL) for an entity.
   * <p>
   * This method allows setting a time-to-live for an entity, after which
   * Redis will automatically remove the entity from the database.
   *
   * @param id         the identifier of the entity
   * @param expiration the expiration duration
   * @param timeUnit   the time unit for the expiration duration
   * @return {@code true} if the expiration was set successfully, {@code false} otherwise
   * @throws IllegalArgumentException if any parameter is null or if expiration is negative
   */
  boolean setExpiration(ID id, Long expiration, TimeUnit timeUnit);

  /**
   * Returns the Redis keyspace (key prefix) used by this repository.
   * <p>
   * The keyspace is typically derived from the entity class name and is used
   * as a prefix for all Redis keys managed by this repository.
   *
   * @return the keyspace string used for Redis keys
   */
  String getKeyspace();

  // QBE Extensions

  /**
   * Updates an entity using Query by Example (QBE) pattern.
   * <p>
   * This method finds an entity matching the example and updates it with
   * the non-null values from the example entity. The example serves both
   * as a query specification and as the source of update values.
   *
   * @param example the example entity containing both query criteria and update values
   * @param <S>     the type of the entity, must be a subtype of T
   * @return the updated entity
   * @throws org.springframework.dao.EmptyResultDataAccessException         if no matching entity is found
   * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if multiple entities match
   */
  <S extends T> S update(Example<S> example);

  /**
   * Updates multiple entities using Query by Example (QBE) patterns.
   * <p>
   * This method performs bulk updates where each example in the iterable
   * serves as both query criteria and update source for matching entities.
   * Each example is processed independently.
   *
   * @param examples an iterable of example entities containing query criteria and update values
   * @param <S>      the type of the entities, must be a subtype of T
   * @throws org.springframework.dao.EmptyResultDataAccessException         if any example matches no entities
   * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if any example matches multiple entities
   */
  <S extends T> void updateAll(Iterable<Example<S>> examples);

  // Key utilities

  /**
   * Generates the Redis key for a given entity.
   * <p>
   * This method constructs the complete Redis key that would be used to store
   * the specified entity, combining the keyspace with the entity's identifier.
   * This is useful for debugging, monitoring, or direct Redis operations.
   *
   * @param entity the entity for which to generate the Redis key
   * @return the complete Redis key string for the entity
   * @throws IllegalArgumentException if the entity is null or has no valid identifier
   */
  String getKeyFor(T entity);
}
