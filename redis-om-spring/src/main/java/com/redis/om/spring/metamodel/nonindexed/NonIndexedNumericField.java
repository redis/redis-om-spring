package com.redis.om.spring.metamodel.nonindexed;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.NumIncrByAction;

/**
 * Represents a non-indexed numeric field in the Redis OM metamodel system.
 * This class provides operations for manipulating numeric fields that are stored
 * in Redis but not included in search indexes. Supports atomic increment and
 * decrement operations.
 * 
 * @param <E> the entity type that contains this field
 * @param <T> the field value type (typically Integer, Long, Double, etc.)
 */
public class NonIndexedNumericField<E, T> extends MetamodelField<E, T> {

  /**
   * Constructs a new NonIndexedNumericField.
   * 
   * @param field   the search field accessor for this field
   * @param indexed whether this field is indexed (should be false for non-indexed fields)
   */
  public NonIndexedNumericField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Creates a consumer that atomically increments this numeric field by the specified value.
   * 
   * @param value the value to increment by
   * @return a consumer that performs the increment operation when applied to an entity
   */
  public Consumer<E> incrBy(Long value) {
    return new NumIncrByAction<>(searchFieldAccessor, value);
  }

  /**
   * Creates a consumer that atomically decrements this numeric field by the specified value.
   * This is implemented as an increment by the negative value.
   * 
   * @param value the value to decrement by
   * @return a consumer that performs the decrement operation when applied to an entity
   */
  public Consumer<E> decrBy(Long value) {
    return new NumIncrByAction<>(searchFieldAccessor, -value);
  }

}
