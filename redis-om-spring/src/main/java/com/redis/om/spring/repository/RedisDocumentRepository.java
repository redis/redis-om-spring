package com.redis.om.spring.repository;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.redis.om.spring.metamodel.MetamodelField;

import redis.clients.jedis.json.Path2;

/**
 * Repository interface for Redis JSON documents providing enhanced functionality
 * over the standard Spring Data Redis repositories.
 * <p>
 * This interface extends {@link KeyValueRepository} and {@link QueryByExampleExecutor}
 * to provide Redis-specific operations for JSON documents stored in Redis using
 * the RedisJSON module. It includes additional methods for bulk operations,
 * field-level updates, expiration handling, and key management.
 * <p>
 * Repositories extending this interface automatically gain full-text search
 * capabilities, vector similarity search, and other Redis Stack features
 * when used with appropriately annotated domain entities.
 *
 * @param <T>  the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @see KeyValueRepository
 * @see QueryByExampleExecutor
 * @since 1.0.0
 */
@NoRepositoryBean
public interface RedisDocumentRepository<T, ID> extends KeyValueRepository<T, ID>, QueryByExampleExecutor<T> {

  /**
   * Returns all entity IDs in the repository.
   * <p>
   * This method retrieves only the identifiers of all entities without
   * loading the full entity data, which can be more efficient for operations
   * that only need to work with IDs.
   *
   * @return an {@link Iterable} of all entity IDs
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
   * Deletes a specific field or nested structure within a JSON document
   * identified by the given ID using a JSONPath expression.
   * <p>
   * This method allows partial deletion of JSON document content without
   * removing the entire document. It uses RedisJSON's JSON.DEL command
   * with a path specification.
   *
   * @param id   the identifier of the entity
   * @param path the JSONPath expression specifying what to delete
   * @throws org.springframework.dao.EmptyResultDataAccessException if no entity with the given ID exists
   */
  void deleteById(ID id, Path2 path);

  /**
   * Updates a specific field of an entity with a new value.
   * <p>
   * This method performs an efficient field-level update using RedisJSON's
   * JSON.SET command, updating only the specified field without affecting
   * other parts of the document.
   *
   * @param entity the entity to update
   * @param field  the metamodel field descriptor identifying which field to update
   * @param value  the new value to set for the field
   * @throws IllegalArgumentException if the entity or field is null
   */
  void updateField(T entity, MetamodelField<T, ?> field, Object value);

  /**
   * Retrieves specific field values from multiple entities by their IDs.
   * <p>
   * This method efficiently extracts a single field from multiple documents
   * using RedisJSON's JSON.GET command with path specification, avoiding
   * the need to load complete entities when only specific field values are needed.
   *
   * @param <F>   the type of the field being retrieved
   * @param ids   the collection of entity IDs
   * @param field the metamodel field descriptor identifying which field to retrieve
   * @return an {@link Iterable} of field values in the same order as the input IDs
   */
  <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field);

  /**
   * Gets the remaining time-to-live (TTL) for an entity in seconds.
   * <p>
   * Returns the number of seconds until the entity expires and is automatically
   * removed from Redis. Returns -1 if the entity exists but has no expiration,
   * or -2 if the entity does not exist.
   *
   * @param id the identifier of the entity
   * @return the TTL in seconds, -1 if no expiration is set, -2 if entity doesn't exist
   */
  Long getExpiration(ID id);

  /**
   * Sets an expiration time for an entity.
   * <p>
   * Configures the entity to be automatically removed from Redis after
   * the specified time period. This is useful for implementing time-based
   * data lifecycle management.
   *
   * @param id         the identifier of the entity
   * @param expiration the expiration time value
   * @param timeUnit   the time unit for the expiration value
   * @return true if the expiration was set successfully, false otherwise
   * @throws IllegalArgumentException if expiration is negative or timeUnit is null
   */
  boolean setExpiration(ID id, Long expiration, TimeUnit timeUnit);

  /**
   * Loads entities in bulk from a file.
   * <p>
   * This method provides efficient bulk loading capabilities for importing
   * large datasets from external files. The file format should contain
   * JSON representations of entities that can be deserialized to the
   * repository's entity type.
   *
   * @param file the path to the file containing entity data
   * @return an {@link Iterable} of loaded entities
   * @throws IOException                                        if there's an error reading the file
   * @throws com.fasterxml.jackson.core.JsonProcessingException if the file contains invalid JSON
   */
  Iterable<T> bulkLoad(String file) throws IOException;

  /**
   * Updates an existing entity in the repository.
   * <p>
   * This method performs a complete entity update, replacing the existing
   * document with the provided entity data. The entity must have a valid
   * identifier that matches an existing document.
   *
   * @param <S>    the type of the entity
   * @param entity the entity to update
   * @return the updated entity
   * @throws IllegalArgumentException                               if the entity is null or has no identifier
   * @throws org.springframework.dao.EmptyResultDataAccessException if no entity with the given ID exists
   */
  <S extends T> S update(S entity);

  /**
   * Returns the Redis keyspace (prefix) used by this repository.
   * <p>
   * The keyspace is the prefix applied to all Redis keys managed by this
   * repository, helping to organize and namespace entities in Redis.
   *
   * @return the keyspace string used as a prefix for all keys
   */
  String getKeyspace();

  // QBE Extensions

  /**
   * Updates an entity using Query by Example (QBE) pattern.
   * <p>
   * This method finds an existing entity that matches the example's
   * non-null properties and updates it with the example's data.
   * This provides a convenient way to update entities when you have
   * partial data or want to use pattern matching.
   *
   * @param <S>     the type of the entity
   * @param example the example entity containing the criteria and update data
   * @return the updated entity
   * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if more than one entity matches the example
   * @throws org.springframework.dao.EmptyResultDataAccessException         if no entity matches the example
   */
  <S extends T> S update(Example<S> example);

  /**
   * Updates multiple entities using Query by Example (QBE) patterns.
   * <p>
   * This method performs bulk updates where each example in the collection
   * is used to find and update matching entities. This is more efficient
   * than calling update for each example individually.
   *
   * @param <S>      the type of the entity
   * @param examples the collection of example entities containing criteria and update data
   * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if any example matches more than one entity
   * @throws org.springframework.dao.EmptyResultDataAccessException         if any example matches no entities
   */
  <S extends T> void updateAll(Iterable<Example<S>> examples);

  // Key utilities

  /**
   * Returns the Redis key that would be used to store the given entity.
   * <p>
   * This utility method constructs the full Redis key (including keyspace prefix)
   * that Redis OM Spring would use to store the provided entity. This is useful
   * for debugging, monitoring, or direct Redis operations.
   *
   * @param entity the entity for which to generate the key
   * @return the complete Redis key string
   * @throws IllegalArgumentException if the entity is null or has no identifier
   */
  String getKeyFor(T entity);
}
