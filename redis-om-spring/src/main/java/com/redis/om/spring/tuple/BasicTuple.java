
package com.redis.om.spring.tuple;

import java.util.stream.Stream;

public interface BasicTuple<R> {

  /**
   * Returns the degree of the BasicTuple. For example, a Tuple2 has a degree of 2
   * whereas a Tuple3 has a degree of 3.
   *
   * @return the degree of the Tuple
   */
  int size();

  /**
   * Gets the tuple element at the given index. For example, get(0) will return
   * the first element and get(1) will return the second etc.
   *
   * @param index of the element to get
   * @return the tuple element at the given index
   * @throws IndexOutOfBoundsException if {@code index < 0 || index >= length()}
   */
  R get(int index);

  /**
   * Returns a {@link Stream} of all values for this Tuple of the given class.
   * I.e. all non-null members of a Tuple that can be cast to the given class are
   * included in the Stream. If sequential, the Stream will start with the 0:th
   * tuple and progress upwards.
   *
   * @param <T>   The type of stream
   * @param clazz The class of the type of the stream
   * @return a {@link Stream} of all values for this Tuple of the given class
   */
  <T> Stream<T> streamOf(Class<T> clazz);

}
