package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * A tuple of fifteen elements.
 *
 * @param <E1>  the type of the first element
 * @param <E2>  the type of the second element
 * @param <E3>  the type of the third element
 * @param <E4>  the type of the fourth element
 * @param <E5>  the type of the fifth element
 * @param <E6>  the type of the sixth element
 * @param <E7>  the type of the seventh element
 * @param <E8>  the type of the eighth element
 * @param <E9>  the type of the ninth element
 * @param <E10> the type of the tenth element
 * @param <E11> the type of the eleventh element
 * @param <E12> the type of the twelfth element
 * @param <E13> the type of the thirteenth element
 * @param <E14> the type of the fourteenth element
 * @param <E15> the type of the fifteenth element
 */
public interface Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> extends Tuple {

  /**
   * Returns a function that extracts the first element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the first element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> FirstAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E1> getFirstGetter() {
    return Quindecuple::getFirst;
  }

  /**
   * Returns a function that extracts the second element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the second element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> SecondAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E2> getSecondGetter() {
    return Quindecuple::getSecond;
  }

  /**
   * Returns a function that extracts the third element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the third element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> ThirdAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E3> getThirdGetter() {
    return Quindecuple::getThird;
  }

  /**
   * Returns a function that extracts the fourth element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the fourth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> FourthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E4> getFourthGetter() {
    return Quindecuple::getFourth;
  }

  /**
   * Returns a function that extracts the fifth element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the fifth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> FifthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E5> getFifthGetter() {
    return Quindecuple::getFifth;
  }

  /**
   * Returns a function that extracts the sixth element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the sixth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> SixthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E6> getSixthGetter() {
    return Quindecuple::getSixth;
  }

  /**
   * Returns a function that extracts the seventh element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the seventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> SeventhAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E7> getSeventhGetter() {
    return Quindecuple::getSeventh;
  }

  /**
   * Returns a function that extracts the eighth element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the eighth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> EighthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E8> getEighthGetter() {
    return Quindecuple::getEighth;
  }

  /**
   * Returns a function that extracts the ninth element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the ninth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> NinthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E9> getNinthGetter() {
    return Quindecuple::getNinth;
  }

  /**
   * Returns a function that extracts the tenth element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the tenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> TenthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E10> getTenthGetter() {
    return Quindecuple::getTenth;
  }

  /**
   * Returns a function that extracts the eleventh element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the eleventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> EleventhAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E11> getEleventhGetter() {
    return Quindecuple::getEleventh;
  }

  /**
   * Returns a function that extracts the twelfth element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the twelfth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> TwelfthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E12> getTwelfthGetter() {
    return Quindecuple::getTwelfth;
  }

  /**
   * Returns a function that extracts the thirteenth element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the thirteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> ThirteenthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E13> getThirteenthGetter() {
    return Quindecuple::getThirteenth;
  }

  /**
   * Returns a function that extracts the fourteenth element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the fourteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> FourteenthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E14> getFourteenthGetter() {
    return Quindecuple::getFourteenth;
  }

  /**
   * Returns a function that extracts the fifteenth element from a Quindecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @param <E12> the type of the twelfth element
   * @param <E13> the type of the thirteenth element
   * @param <E14> the type of the fourteenth element
   * @param <E15> the type of the fifteenth element
   * @return a function that extracts the fifteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> FifteenthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E15> getFifteenthGetter() {
    return Quindecuple::getFifteenth;
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

  /**
   * Returns the tenth element of this tuple.
   *
   * @return the tenth element
   */
  E10 getTenth();

  /**
   * Returns the eleventh element of this tuple.
   *
   * @return the eleventh element
   */
  E11 getEleventh();

  /**
   * Returns the twelfth element of this tuple.
   *
   * @return the twelfth element
   */
  E12 getTwelfth();

  /**
   * Returns the thirteenth element of this tuple.
   *
   * @return the thirteenth element
   */
  E13 getThirteenth();

  /**
   * Returns the fourteenth element of this tuple.
   *
   * @return the fourteenth element
   */
  E14 getFourteenth();

  /**
   * Returns the fifteenth element of this tuple.
   *
   * @return the fifteenth element
   */
  E15 getFifteenth();

  @Override
  default int size() {
    return 15;
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
      case 9 -> getTenth();
      case 10 -> getEleventh();
      case 11 -> getTwelfth();
      case 12 -> getThirteenth();
      case 13 -> getFourteenth();
      case 14 -> getFifteenth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}