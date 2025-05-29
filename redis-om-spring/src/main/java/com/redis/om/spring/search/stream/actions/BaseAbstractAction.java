package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;

/**
 * Abstract base class for all Redis JSON action implementations.
 * This class provides common functionality for actions that operate on JSON documents
 * stored in Redis, including field access, entity class management, and JSON operations.
 * 
 * <p>BaseAbstractAction serves as the foundation for all concrete action classes
 * that perform operations on Redis JSON arrays and documents. It handles the basic
 * infrastructure needed for entity identification and JSON operations.</p>
 * 
 * <p>All actions require a {@link SearchFieldAccessor} that identifies the specific
 * field within an entity to operate on, and they validate that the entity class
 * has a proper ID field for Redis key generation.</p>
 * 
 * @since 1.0
 * @see TakesJSONOperations
 * @see SearchFieldAccessor
 */
public abstract class BaseAbstractAction implements TakesJSONOperations {
  /** The field accessor that identifies the target field for this action */
  protected final SearchFieldAccessor field;

  /** The JSON operations instance for executing Redis JSON commands */
  protected JSONOperations<String> json;

  /** The ID field for the entity class, used for generating Redis keys */
  protected Field idField;

  /**
   * Constructs a new BaseAbstractAction for the specified field.
   * This constructor validates that the entity class has an ID field,
   * which is required for generating Redis keys.
   * 
   * @param field the search field accessor that identifies the target field
   * @throws NullPointerException if the entity class does not have an ID field
   */
  protected BaseAbstractAction(SearchFieldAccessor field) {
    this.field = field;
    Class<?> entityClass = field.getDeclaringClass();
    Optional<Field> maybeId = ObjectUtils.getIdFieldForEntityClass(entityClass);
    if (maybeId.isPresent()) {
      this.idField = maybeId.get();
    } else {
      throw new NullPointerException(String.format("Entity Class %s does not have an ID field", entityClass
          .getSimpleName()));
    }
  }

  @Override
  public void setJSONOperations(JSONOperations<String> json) {
    this.json = json;
  }

  /**
   * Generates the Redis key for the specified entity.
   * The key is constructed using the entity's class name and ID value.
   * 
   * @param entity the entity to generate a key for
   * @return the Redis key for the entity
   */
  protected String getKey(Object entity) {
    String id = ObjectUtils.getIdFieldForEntity(idField, entity).toString();
    return field.getDeclaringClass().getName() + ":" + id;
  }
}
