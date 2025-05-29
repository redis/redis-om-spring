package com.redis.om.spring.metamodel.indexed;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.ToggleAction;

/**
 * Metamodel field representing a boolean property in a Redis OM Spring entity.
 * <p>
 * This field type extends {@link TagField} to provide boolean-specific operations
 * while maintaining the tag-based indexing capabilities. Boolean fields are indexed
 * as tags in Redis, allowing for efficient exact-match queries.
 * </p>
 * <p>
 * The BooleanField provides specialized operations such as value toggling,
 * which is particularly useful for boolean properties in entity update operations.
 * </p>
 *
 * @param <E> the entity type containing this field
 * @param <T> the field value type (typically Boolean)
 * @see TagField
 * @see com.redis.om.spring.search.stream.actions.ToggleAction
 * @since 0.1.0
 */
public class BooleanField<E, T> extends TagField<E, T> {
  /**
   * Creates a BooleanField with the specified field accessor and indexing status.
   *
   * @param field   the field accessor for reading/writing the field value
   * @param indexed whether this field is indexed for searching
   */
  public BooleanField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Creates a BooleanField for the specified entity class and field name.
   *
   * @param targetClass the entity class containing this field
   * @param fieldName   the name of the field
   */
  public BooleanField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  /**
   * Creates a consumer that toggles the boolean value of this field.
   * <p>
   * This operation flips the boolean value: {@code true} becomes {@code false}
   * and {@code false} becomes {@code true}. This is useful for entity update
   * operations where you need to toggle boolean flags.
   * </p>
   *
   * @return a consumer that toggles the field value when applied to an entity
   */
  public Consumer<E> toggle() {
    return new ToggleAction<>(searchFieldAccessor);
  }
}
