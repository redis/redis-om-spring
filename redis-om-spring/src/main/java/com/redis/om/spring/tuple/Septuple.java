package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * Represents a tuple of seven elements.
 * <p>
 * This interface extends {@link Tuple} and provides access to seven typed elements
 * through getter methods and accessor functions.
 * </p>
 *
 * @param <E1> the type of the first element
 * @param <E2> the type of the second element
 * @param <E3> the type of the third element
 * @param <E4> the type of the fourth element
 * @param <E5> the type of the fifth element
 * @param <E6> the type of the sixth element
 * @param <E7> the type of the seventh element
 */
public interface Septuple<E1, E2, E3, E4, E5, E6, E7> extends Tuple {

  /**
   * Returns a function that extracts the first element from a Septuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @return a function that extracts the first element
   */
  static <E1, E2, E3, E4, E5, E6, E7> FirstAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E1> getFirstGetter() {
    return Septuple::getFirst;
  }

  /**
   * Returns a function that extracts the second element from a Septuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @return a function that extracts the second element
   */
  static <E1, E2, E3, E4, E5, E6, E7> SecondAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E2> getSecondGetter() {
    return Septuple::getSecond;
  }

  /**
   * Returns a function that extracts the third element from a Septuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @return a function that extracts the third element
   */
  static <E1, E2, E3, E4, E5, E6, E7> ThirdAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E3> getThirdGetter() {
    return Septuple::getThird;
  }

  /**
   * Returns a function that extracts the fourth element from a Septuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @return a function that extracts the fourth element
   */
  static <E1, E2, E3, E4, E5, E6, E7> FourthAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E4> getFourthGetter() {
    return Septuple::getFourth;
  }

  /**
   * Returns a function that extracts the fifth element from a Septuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @return a function that extracts the fifth element
   */
  static <E1, E2, E3, E4, E5, E6, E7> FifthAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E5> getFifthGetter() {
    return Septuple::getFifth;
  }

  /**
   * Returns a function that extracts the sixth element from a Septuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @return a function that extracts the sixth element
   */
  static <E1, E2, E3, E4, E5, E6, E7> SixthAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E6> getSixthGetter() {
    return Septuple::getSixth;
  }

  /**
   * Returns a function that extracts the seventh element from a Septuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @return a function that extracts the seventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7> SeventhAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E7> getSeventhGetter() {
    return Septuple::getSeventh;
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

  /**
   * Returns the sixth element of this tuple.
   *
   * @return the sixth element
   */
  E6 getSixth();

  /**
   * Returns the seventh element of this tuple.
   *
   * @return the seventh element
   */
  E7 getSeventh();

  @Override
  default int size() {
    return 7;
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
      case 5 -> getSixth();
      case 6 -> getSeventh();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}