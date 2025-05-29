package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * Represents a tuple of five elements.
 * <p>
 * This interface extends {@link Tuple} and provides access to five typed elements
 * through getter methods and accessor functions.
 * </p>
 *
 * @param <E1> the type of the first element
 * @param <E2> the type of the second element
 * @param <E3> the type of the third element
 * @param <E4> the type of the fourth element
 * @param <E5> the type of the fifth element
 */
public interface Quintuple<E1, E2, E3, E4, E5> extends Tuple {

  /**
   * Returns a function that extracts the first element from a Quintuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @return a function that extracts the first element
   */
  static <E1, E2, E3, E4, E5> FirstAccessor<Quintuple<E1, E2, E3, E4, E5>, E1> getFirstGetter() {
    return Quintuple::getFirst;
  }

  /**
   * Returns a function that extracts the second element from a Quintuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @return a function that extracts the second element
   */
  static <E1, E2, E3, E4, E5> SecondAccessor<Quintuple<E1, E2, E3, E4, E5>, E2> getSecondGetter() {
    return Quintuple::getSecond;
  }

  /**
   * Returns a function that extracts the third element from a Quintuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @return a function that extracts the third element
   */
  static <E1, E2, E3, E4, E5> ThirdAccessor<Quintuple<E1, E2, E3, E4, E5>, E3> getThirdGetter() {
    return Quintuple::getThird;
  }

  /**
   * Returns a function that extracts the fourth element from a Quintuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @return a function that extracts the fourth element
   */
  static <E1, E2, E3, E4, E5> FourthAccessor<Quintuple<E1, E2, E3, E4, E5>, E4> getFourthGetter() {
    return Quintuple::getFourth;
  }

  /**
   * Returns a function that extracts the fifth element from a Quintuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @return a function that extracts the fifth element
   */
  static <E1, E2, E3, E4, E5> FifthAccessor<Quintuple<E1, E2, E3, E4, E5>, E5> getFifthGetter() {
    return Quintuple::getFifth;
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

  /**
   * Returns the fifth element of this tuple.
   *
   * @return the fifth element
   */
  E5 getFifth();

  @Override
  default int size() {
    return 5;
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
      case 4 -> getFifth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}