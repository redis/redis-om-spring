
package com.redis.om.spring.tuple;

import java.util.function.Function;

/**
 * A BasicTupleMapper may be used to map from an object of type T to a Tuple
 *
 * @param <R> The return type of the apply method
 */
public interface TupleMapper<T, R> extends Function<T, R> {

  /**
   * Returns the degree of the Tuple. For example, a Tuple2 has a degree of 2
   * whereas a Tuple3 has a degree of 3.
   *
   * @return the degree of the Tuple
   */
  int degree();

  /**
   * Gets the mapper at the given index. For example, get(0) will return the first
   * mapper and get(1) will return the second etc.
   *
   * @param index of the mapper to get
   * @return the mapper at the given index
   * @throws IndexOutOfBoundsException if {@code index < 0 || index >= length()}
   */
  Function<T, ?> get(int index);

}
