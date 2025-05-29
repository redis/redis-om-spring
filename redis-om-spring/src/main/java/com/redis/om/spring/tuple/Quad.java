
package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

/**
 * Represents a tuple of four elements.
 * <p>
 * This interface extends {@link Tuple} and provides access to four typed elements
 * through getter methods and accessor functions.
 * </p>
 *
 * @param <E1> the type of the first element
 * @param <E2> the type of the second element
 * @param <E3> the type of the third element
 * @param <E4> the type of the fourth element
 */
public interface Quad<E1, E2, E3, E4> extends Tuple {

  /**
   * Returns a function that extracts the first element from a Quad.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @return a function that extracts the first element
   */
  static <E1, E2, E3, E4> FirstAccessor<Quad<E1, E2, E3, E4>, E1> getFirstGetter() {
    return Quad::getFirst;
  }

  /**
   * Returns a function that extracts the second element from a Quad.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @return a function that extracts the second element
   */
  static <E1, E2, E3, E4> SecondAccessor<Quad<E1, E2, E3, E4>, E2> getSecondGetter() {
    return Quad::getSecond;
  }

  /**
   * Returns a function that extracts the third element from a Quad.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @return a function that extracts the third element
   */
  static <E1, E2, E3, E4> ThirdAccessor<Quad<E1, E2, E3, E4>, E3> getThirdGetter() {
    return Quad::getThird;
  }

  /**
   * Returns a function that extracts the fourth element from a Quad.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @return a function that extracts the fourth element
   */
  static <E1, E2, E3, E4> FourthAccessor<Quad<E1, E2, E3, E4>, E4> getFourthGetter() {
    return Quad::getFourth;
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

  /**
   * Returns the third element of this tuple.
   *
   * @return the third element
   */
  E3 getThird();

  /**
   * Returns the fourth element of this tuple.
   *
   * @return the fourth element
   */
  E4 getFourth();

  @Override
  default int size() {
    return 4;
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
      case 2 -> getThird();
      case 3 -> getFourth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}