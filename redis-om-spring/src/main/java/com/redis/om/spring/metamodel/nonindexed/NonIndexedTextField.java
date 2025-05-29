package com.redis.om.spring.metamodel.nonindexed;

import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.StrLengthAction;
import com.redis.om.spring.search.stream.actions.StringAppendAction;

/**
 * Represents a non-indexed text field in the Redis OM metamodel system.
 * This class provides operations for manipulating string fields that are stored
 * in Redis but not included in search indexes. Supports string manipulation
 * operations such as appending text and getting string length.
 * 
 * @param <E> the entity type that contains this field
 * @param <T> the field value type (typically String)
 */
public class NonIndexedTextField<E, T> extends MetamodelField<E, T> {

  /**
   * Constructs a new NonIndexedTextField.
   * 
   * @param field   the search field accessor for this field
   * @param indexed whether this field is indexed (should be false for non-indexed fields)
   */
  public NonIndexedTextField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Creates a consumer that appends the specified string value to this text field.
   * 
   * @param value the string value to append
   * @return a consumer that performs the append operation when applied to an entity
   */
  public Consumer<? super E> append(String value) {
    return new StringAppendAction<>(searchFieldAccessor, value);
  }

  /**
   * Creates a function that returns the length of this text field.
   * 
   * @return a function that returns the string length when applied to an entity
   */
  public ToLongFunction<? super E> length() {
    return new StrLengthAction<>(searchFieldAccessor);
  }

}
