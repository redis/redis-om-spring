package com.redis.om.spring.metamodel.nonindexed;

import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.*;

/**
 * Represents a non-indexed tag field in the Redis OM metamodel system.
 * This class provides operations for manipulating collection/array fields that are stored
 * in Redis but not included in search indexes. Supports array-like operations such as
 * adding, inserting, removing elements, and querying array properties.
 * 
 * @param <E> the entity type that contains this field
 * @param <T> the field value type (typically a collection or array)
 */
public class NonIndexedTagField<E, T> extends MetamodelField<E, T> {

  /**
   * Constructs a new NonIndexedTagField.
   * 
   * @param field   the search field accessor for this field
   * @param indexed whether this field is indexed (should be false for non-indexed fields)
   */
  public NonIndexedTagField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Creates a consumer that appends a value to the end of this array field.
   * 
   * @param value the value to append
   * @return a consumer that performs the append operation when applied to an entity
   */
  public Consumer<? super E> add(Object value) {
    return new ArrayAppendAction<>(searchFieldAccessor, value);
  }

  /**
   * Creates a consumer that inserts a value at the specified index in this array field.
   * 
   * @param value the value to insert
   * @param index the index at which to insert the value
   * @return a consumer that performs the insert operation when applied to an entity
   */
  public Consumer<? super E> insert(Object value, Integer index) {
    return new ArrayInsertAction<>(searchFieldAccessor, value, index);
  }

  /**
   * Creates a consumer that prepends a value to the beginning of this array field.
   * This is equivalent to inserting at index 0.
   * 
   * @param value the value to prepend
   * @return a consumer that performs the prepend operation when applied to an entity
   */
  public Consumer<? super E> prepend(Object value) {
    return new ArrayInsertAction<>(searchFieldAccessor, value, 0);
  }

  /**
   * Creates a function that returns the length of this array field.
   * 
   * @return a function that returns the array length when applied to an entity
   */
  public ToLongFunction<? super E> length() {
    return new ArrayLengthAction<>(searchFieldAccessor);
  }

  /**
   * Creates a function that returns the index of the first occurrence of the specified element
   * in this array field.
   * 
   * @param element the element to search for
   * @return a function that returns the index of the element when applied to an entity,
   *         or -1 if the element is not found
   */
  public ToLongFunction<? super E> indexOf(Object element) {
    return new ArrayIndexOfAction<>(searchFieldAccessor, element);
  }

  /**
   * Creates an action that removes and returns the element at the specified index
   * from this array field.
   * 
   * @param <R>   the return type of the popped element
   * @param index the index of the element to remove
   * @return an action that performs the pop operation when applied to an entity
   */
  public <R> ArrayPopAction<? super E, R> pop(Integer index) {
    return new ArrayPopAction<>(searchFieldAccessor, index);
  }

  /**
   * Creates an action that removes and returns the last element from this array field.
   * This is equivalent to popping at index -1.
   * 
   * @param <R> the return type of the popped element
   * @return an action that performs the pop operation when applied to an entity
   */
  public <R> ArrayPopAction<? super E, R> pop() {
    return pop(-1);
  }

  /**
   * Creates an action that removes and returns the first element from this array field.
   * This is equivalent to popping at index 0.
   * 
   * @param <R> the return type of the removed element
   * @return an action that performs the remove operation when applied to an entity
   */
  public <R> ArrayPopAction<? super E, R> removeFirst() {
    return pop(0);
  }

  /**
   * Creates an action that removes and returns the last element from this array field.
   * This is equivalent to popping at index -1.
   * 
   * @param <R> the return type of the removed element
   * @return an action that performs the remove operation when applied to an entity
   */
  public <R> ArrayPopAction<? super E, R> removeLast() {
    return pop(-1);
  }

  /**
   * Creates an action that removes and returns the element at the specified index
   * from this array field. This is equivalent to the pop operation.
   * 
   * @param <R>   the return type of the removed element
   * @param index the index of the element to remove
   * @return an action that performs the remove operation when applied to an entity
   */
  public <R> ArrayPopAction<? super E, R> remove(Integer index) {
    return pop(index);
  }

  /**
   * Creates a consumer that trims this array field to keep only elements within
   * the specified range (inclusive of begin, exclusive of end).
   * 
   * @param begin the starting index (inclusive)
   * @param end   the ending index (exclusive)
   * @return a consumer that performs the trim operation when applied to an entity
   */
  public Consumer<? super E> trimToRange(Integer begin, Integer end) {
    return new ArrayTrimAction<>(searchFieldAccessor, begin, end);
  }

}