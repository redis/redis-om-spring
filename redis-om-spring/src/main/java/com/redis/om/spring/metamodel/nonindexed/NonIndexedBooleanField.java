package com.redis.om.spring.metamodel.nonindexed;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.ToggleAction;

/**
 * Represents a non-indexed boolean field in the Redis OM metamodel system.
 * This class provides operations for manipulating boolean fields that are stored
 * in Redis but not included in search indexes.
 * 
 * @param <E> the entity type that contains this field
 * @param <T> the field value type (typically Boolean)
 */
public class NonIndexedBooleanField<E, T> extends MetamodelField<E, T> {
  /**
   * Constructs a new NonIndexedBooleanField.
   * 
   * @param field   the search field accessor for this field
   * @param indexed whether this field is indexed (should be false for non-indexed fields)
   */
  public NonIndexedBooleanField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Creates a consumer that toggles the boolean value of this field.
   * When applied to an entity, this will flip the boolean value from true to false or vice versa.
   * 
   * @return a consumer that performs the toggle operation when applied to an entity
   */
  public Consumer<E> toggle() {
    return new ToggleAction<>(searchFieldAccessor);
  }
}
