package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FirstAccessor;

/**
 * Represents a tuple of one element.
 * <p>
 * This interface extends {@link Tuple} and provides access to a single typed element
 * through a getter method and accessor function. While a single-element tuple may seem
 * redundant, it provides consistency in tuple-based APIs and can be useful for
 * generic programming scenarios.
 * </p>
 *
 * @param <T0> the type of the first (and only) element
 */
public interface Single<T0> extends Tuple {

  /**
   * Returns a function that extracts the first element from a Single.
   *
   * @param <T0> the type of the first element
   * @return a function that extracts the first element
   */
  static <T0> FirstAccessor<Single<T0>, T0> getFirstGetter() {
    return Single::getFirst;
  }

  /**
   * Returns the first (and only) element of this tuple.
   *
   * @return the first element
   */
  T0 getFirst();

  @Override
  default int size() {
    return 1;
  }

  /**
   * Returns the element at the specified index.
   * For a Single tuple, only index 0 is valid.
   *
   * @param index the index of the element to return (must be 0)
   * @return the element at the specified index
   * @throws IndexOutOfBoundsException if the index is not 0
   */
  default Object get(int index) {
    if (index == 0) {
      return getFirst();
    } else {
      throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s", index,
          size()));
    }
  }
}