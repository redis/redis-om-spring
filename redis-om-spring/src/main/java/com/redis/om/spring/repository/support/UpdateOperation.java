package com.redis.om.spring.repository.support;

import com.redis.om.spring.metamodel.MetamodelField;

/**
 * Represents an atomic update operation for a specific field in a Redis entity.
 * <p>
 * This class encapsulates the information needed to perform a field-level update
 * on a Redis document or hash structure. It is used internally by the repository
 * infrastructure to batch and execute partial updates efficiently without requiring
 * a full entity read-modify-write cycle.
 * </p>
 * <p>
 * Update operations are particularly useful for:
 * <ul>
 * <li>Incrementing numeric fields without race conditions</li>
 * <li>Updating individual fields without affecting others</li>
 * <li>Performing bulk updates across multiple entities</li>
 * <li>Optimizing write performance by reducing network round trips</li>
 * </ul>
 * 
 * @see com.redis.om.spring.repository.RedisDocumentRepository
 * @see com.redis.om.spring.repository.RedisEnhancedRepository
 * @since 0.1.0
 */
public class UpdateOperation {
  /**
   * The Redis key identifying the entity to update.
   */
  final String key;

  /**
   * The metamodel field descriptor containing field metadata and path information.
   */
  final MetamodelField<?, ?> field;

  /**
   * The new value to set for the field.
   */
  final Object value;

  /**
   * Constructs a new update operation.
   * 
   * @param key   the Redis key of the entity to update
   * @param field the metamodel field to update
   * @param value the new value for the field
   */
  UpdateOperation(String key, MetamodelField<?, ?> field, Object value) {
    this.key = key;
    this.field = field;
    this.value = value;
  }
}
