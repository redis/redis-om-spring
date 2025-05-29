package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;

/**
 * Represents a tuple of two elements.
 * <p>
 * This interface extends {@link Tuple} and provides access to two typed elements
 * through getter methods and accessor functions.
 * </p>
 *
 * @param <E1> the type of the first element
 * @param <E2> the type of the second element
 */
public interface Pair<E1, E2> extends Tuple {

  /**
   * Returns a function that extracts the first element from a Pair.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @return a function that extracts the first element
   */
  static <E1, E2> FirstAccessor<Pair<E1, E2>, E1> getFirstGetter() {
    return Pair::getFirst;
  }

  /**
   * Returns a function that extracts the second element from a Pair.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @return a function that extracts the second element
   */
  static <E1, E2> SecondAccessor<Pair<E1, E2>, E2> getSecondGetter() {
    return Pair::getSecond;
  }

  /**
   * Returns the first element of this tuple.
   *
   * @return the first element
   */
  E1 getFirst();

  /**
   * Returns the second element of this tuple.
   *
   * @return the second element
   */
  E2 getSecond();

  @Override
  default int size() {
    return 2;
  }

  /**
   * Returns the element at the specified index.
   *
   * @param index the index of the element to return (0-based)
   * @return the element at the specified index
   * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size())
   */
  default Object get(int index) {
    return switch (index) {
      case 0 -> getFirst();
      case 1 -> getSecond();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}