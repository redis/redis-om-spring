package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * Represents a tuple of eight elements.
 * <p>
 * This interface extends {@link Tuple} and provides access to eight typed elements
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
 * @param <T7> the type of the eighth element
 */
public interface Octuple<E1, E2, E3, E4, E5, E6, E7, T7> extends Tuple {

  /**
   * Returns a function that extracts the first element from an Octuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @return a function that extracts the first element
   */
  static <E1, E2, E3, E4, E5, E6, E7, T7> FirstAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E1> getFirstGetter() {
    return Octuple::getFirst;
  }

  /**
   * Returns a function that extracts the second element from an Octuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @return a function that extracts the second element
   */
  static <E1, E2, E3, E4, E5, E6, E7, T7> SecondAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E2> getSecondGetter() {
    return Octuple::getSecond;
  }

  /**
   * Returns a function that extracts the third element from an Octuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @return a function that extracts the third element
   */
  static <E1, E2, E3, E4, E5, E6, E7, T7> ThirdAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E3> getThirdGetter() {
    return Octuple::getThird;
  }

  /**
   * Returns a function that extracts the fourth element from an Octuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @return a function that extracts the fourth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, T7> FourthAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E4> getFourthGetter() {
    return Octuple::getFourth;
  }

  /**
   * Returns a function that extracts the fifth element from an Octuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @return a function that extracts the fifth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, T7> FifthAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E5> getFifthGetter() {
    return Octuple::getFifth;
  }

  /**
   * Returns a function that extracts the sixth element from an Octuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @return a function that extracts the sixth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, T7> SixthAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E6> getSixthGetter() {
    return Octuple::getSixth;
  }

  /**
   * Returns a function that extracts the seventh element from an Octuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @return a function that extracts the seventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, T7> SeventhAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E7> getSeventhGetter() {
    return Octuple::getSeventh;
  }

  /**
   * Returns a function that extracts the eighth element from an Octuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @return a function that extracts the eighth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, T7> EighthAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, T7> getEighthGetter() {
    return Octuple::getEighth;
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

  /**
   * Returns the eighth element of this tuple.
   *
   * @return the eighth element
   */
  T7 getEighth();

  @Override
  default int size() {
    return 8;
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
      case 7 -> getEighth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}