package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * A tuple of degree 9 holding nine elements.
 *
 * @param <E1> the type of the first element
 * @param <E2> the type of the second element
 * @param <E3> the type of the third element
 * @param <E4> the type of the fourth element
 * @param <E5> the type of the fifth element
 * @param <E6> the type of the sixth element
 * @param <E7> the type of the seventh element
 * @param <E8> the type of the eighth element
 * @param <E9> the type of the ninth element
 */
public interface Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9> extends Tuple {

  /**
   * Returns a function that can be used to access the first element of a Nonuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <E8> the type of the eighth element
   * @param <E9> the type of the ninth element
   * @return a function to access the first element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> FirstAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E1> getFirstGetter() {
    return Nonuple::getFirst;
  }

  /**
   * Returns a function that can be used to access the second element of a Nonuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <E8> the type of the eighth element
   * @param <E9> the type of the ninth element
   * @return a function to access the second element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> SecondAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E2> getSecondGetter() {
    return Nonuple::getSecond;
  }

  /**
   * Returns a function that can be used to access the third element of a Nonuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <E8> the type of the eighth element
   * @param <E9> the type of the ninth element
   * @return a function to access the third element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> ThirdAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E3> getThirdGetter() {
    return Nonuple::getThird;
  }

  /**
   * Returns a function that can be used to access the fourth element of a Nonuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <E8> the type of the eighth element
   * @param <E9> the type of the ninth element
   * @return a function to access the fourth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> FourthAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E4> getFourthGetter() {
    return Nonuple::getFourth;
  }

  /**
   * Returns a function that can be used to access the fifth element of a Nonuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <E8> the type of the eighth element
   * @param <E9> the type of the ninth element
   * @return a function to access the fifth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> FifthAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E5> getFifthGetter() {
    return Nonuple::getFifth;
  }

  /**
   * Returns a function that can be used to access the sixth element of a Nonuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <E8> the type of the eighth element
   * @param <E9> the type of the ninth element
   * @return a function to access the sixth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> SixthAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E6> getSixthGetter() {
    return Nonuple::getSixth;
  }

  /**
   * Returns a function that can be used to access the seventh element of a Nonuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <E8> the type of the eighth element
   * @param <E9> the type of the ninth element
   * @return a function to access the seventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> SeventhAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E7> getSeventhGetter() {
    return Nonuple::getSeventh;
  }

  /**
   * Returns a function that can be used to access the eighth element of a Nonuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <E8> the type of the eighth element
   * @param <E9> the type of the ninth element
   * @return a function to access the eighth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> EighthAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E8> getEighthGetter() {
    return Nonuple::getEighth;
  }

  /**
   * Returns a function that can be used to access the ninth element of a Nonuple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @param <E4> the type of the fourth element
   * @param <E5> the type of the fifth element
   * @param <E6> the type of the sixth element
   * @param <E7> the type of the seventh element
   * @param <E8> the type of the eighth element
   * @param <E9> the type of the ninth element
   * @return a function to access the ninth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> NinthAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E9> getNinthGetter() {
    return Nonuple::getNinth;
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
  E8 getEighth();

  /**
   * Returns the ninth element of this tuple.
   *
   * @return the ninth element
   */
  E9 getNinth();

  @Override
  default int size() {
    return 9;
  }

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
      case 8 -> getNinth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}