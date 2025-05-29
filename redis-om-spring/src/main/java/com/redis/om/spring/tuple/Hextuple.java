package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * A tuple of degree 6 holding six elements.
 *
 * @param <E1> the type of the first element
 * @param <E2> the type of the second element
 * @param <E3> the type of the third element
 * @param <E4> the type of the fourth element
 * @param <E5> the type of the fifth element
 * @param <E6> the type of the sixth element
 */
public interface Hextuple<E1, E2, E3, E4, E5, E6> extends Tuple {

  /**
   * Returns a function that can be used to access the first element of a Hextuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @return a function to access the first element
   */
  static <E1, E2, E3, E4, E5, E6> FirstAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E1> getFirstGetter() {
    return Hextuple::getFirst;
  }

  /**
   * Returns a function that can be used to access the second element of a Hextuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @return a function to access the second element
   */
  static <E1, E2, E3, E4, E5, E6> SecondAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E2> getSecondGetter() {
    return Hextuple::getSecond;
  }

  /**
   * Returns a function that can be used to access the third element of a Hextuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @return a function to access the third element
   */
  static <E1, E2, E3, E4, E5, E6> ThirdAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E3> getThirdGetter() {
    return Hextuple::getThird;
  }

  /**
   * Returns a function that can be used to access the fourth element of a Hextuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @return a function to access the fourth element
   */
  static <E1, E2, E3, E4, E5, E6> FourthAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E4> getFourthGetter() {
    return Hextuple::getFourth;
  }

  /**
   * Returns a function that can be used to access the fifth element of a Hextuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @return a function to access the fifth element
   */
  static <E1, E2, E3, E4, E5, E6> FifthAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E5> getFifthGetter() {
    return Hextuple::getFifth;
  }

  /**
   * Returns a function that can be used to access the sixth element of a Hextuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @return a function to access the sixth element
   */
  static <E1, E2, E3, E4, E5, E6> SixthAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E6> getSixthGetter() {
    return Hextuple::getSixth;
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

  @Override
  default int size() {
    return 6;
  }

  default Object get(int index) {
    return switch (index) {
      case 0 -> getFirst();
      case 1 -> getSecond();
      case 2 -> getThird();
      case 3 -> getFourth();
      case 4 -> getFifth();
      case 5 -> getSixth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}