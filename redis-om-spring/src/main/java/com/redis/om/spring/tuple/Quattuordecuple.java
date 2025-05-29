package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * A tuple of fourteen elements.
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
 */
public interface Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> extends Tuple {

  /**
   * Returns a function that extracts the first element from a Quattuordecuple.
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
   * @return a function that extracts the first element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> FirstAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E1> getFirstGetter() {
    return Quattuordecuple::getFirst;
  }

  /**
   * Returns a function that extracts the second element from a Quattuordecuple.
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
   * @return a function that extracts the second element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> SecondAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E2> getSecondGetter() {
    return Quattuordecuple::getSecond;
  }

  /**
   * Returns a function that extracts the third element from a Quattuordecuple.
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
   * @return a function that extracts the third element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> ThirdAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E3> getThirdGetter() {
    return Quattuordecuple::getThird;
  }

  /**
   * Returns a function that extracts the fourth element from a Quattuordecuple.
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
   * @return a function that extracts the fourth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> FourthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E4> getFourthGetter() {
    return Quattuordecuple::getFourth;
  }

  /**
   * Returns a function that extracts the fifth element from a Quattuordecuple.
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
   * @return a function that extracts the fifth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> FifthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E5> getFifthGetter() {
    return Quattuordecuple::getFifth;
  }

  /**
   * Returns a function that extracts the sixth element from a Quattuordecuple.
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
   * @return a function that extracts the sixth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> SixthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E6> getSixthGetter() {
    return Quattuordecuple::getSixth;
  }

  /**
   * Returns a function that extracts the seventh element from a Quattuordecuple.
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
   * @return a function that extracts the seventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> SeventhAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E7> getSeventhGetter() {
    return Quattuordecuple::getSeventh;
  }

  /**
   * Returns a function that extracts the eighth element from a Quattuordecuple.
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
   * @return a function that extracts the eighth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> EighthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E8> getEighthGetter() {
    return Quattuordecuple::getEighth;
  }

  /**
   * Returns a function that extracts the ninth element from a Quattuordecuple.
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
   * @return a function that extracts the ninth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> NinthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E9> getNinthGetter() {
    return Quattuordecuple::getNinth;
  }

  /**
   * Returns a function that extracts the tenth element from a Quattuordecuple.
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
   * @return a function that extracts the tenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> TenthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E10> getTenthGetter() {
    return Quattuordecuple::getTenth;
  }

  /**
   * Returns a function that extracts the eleventh element from a Quattuordecuple.
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
   * @return a function that extracts the eleventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> EleventhAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E11> getEleventhGetter() {
    return Quattuordecuple::getEleventh;
  }

  /**
   * Returns a function that extracts the twelfth element from a Quattuordecuple.
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
   * @return a function that extracts the twelfth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> TwelfthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E12> getTwelfthGetter() {
    return Quattuordecuple::getTwelfth;
  }

  /**
   * Returns a function that extracts the thirteenth element from a Quattuordecuple.
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
   * @return a function that extracts the thirteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> ThirteenthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E13> getThirteenthGetter() {
    return Quattuordecuple::getThirteenth;
  }

  /**
   * Returns a function that extracts the fourteenth element from a Quattuordecuple.
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
   * @return a function that extracts the fourteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> FourteenthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E14> getFourteenthGetter() {
    return Quattuordecuple::getFourteenth;
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

  @Override
  default int size() {
    return 14;
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
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}