package com.redis.om.spring.tuple;

import java.util.Map;
import java.util.stream.Stream;

/**
 * A generic tuple interface that provides a contract for accessing elements of a tuple-like data structure.
 * This interface defines the fundamental operations for working with tuple objects that contain an ordered
 * collection of elements of type R.
 * 
 * <p>GenericTuple provides type-safe access to tuple elements and supports streaming operations for
 * filtering and accessing elements by type. It also supports creating labeled maps from tuple data.</p>
 * 
 * <p>Implementations of this interface should maintain immutability and thread-safety where possible.</p>
 * 
 * @param <R> the type of elements contained in this tuple
 * 
 * @since 1.0
 */
public interface GenericTuple<R> {
  /**
   * Returns the number of elements in this tuple.
   * 
   * @return the size of this tuple, always non-negative
   */
  int size();

  /**
   * Retrieves the element at the specified index in this tuple.
   * 
   * @param index the zero-based index of the element to retrieve
   * @return the element at the specified index
   * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size())
   */
  R get(int index);

  /**
   * Returns a stream containing only the elements of this tuple that are instances of the specified class.
   * This method filters the tuple elements by type and provides type-safe access to elements
   * of a specific type.
   * 
   * @param <T>   the type to filter by
   * @param clazz the class object representing the type to filter by
   * @return a stream of elements that are instances of the specified class, never null
   * @throws NullPointerException if clazz is null
   */
  <T> Stream<T> streamOf(Class<T> clazz);

  /**
   * Returns a map representation of this tuple with string labels as keys and tuple elements as values.
   * The map provides a way to access tuple elements by meaningful names rather than numeric indices.
   * 
   * <p>The returned map should be immutable to preserve tuple semantics. If no labels are available
   * or the tuple is empty, an empty map is returned.</p>
   * 
   * @return an immutable map containing labeled tuple elements, never null
   */
  Map<String, Object> labelledMap();

}
