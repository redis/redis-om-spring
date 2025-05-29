package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * A tuple of degree 18 that holds eighteen elements.
 *
 * @param <E1>  Type of the first element
 * @param <E2>  Type of the second element
 * @param <E3>  Type of the third element
 * @param <E4>  Type of the fourth element
 * @param <E5>  Type of the fifth element
 * @param <E6>  Type of the sixth element
 * @param <E7>  Type of the seventh element
 * @param <E8>  Type of the eighth element
 * @param <E9>  Type of the ninth element
 * @param <E10> Type of the tenth element
 * @param <E11> Type of the eleventh element
 * @param <E12> Type of the twelfth element
 * @param <E13> Type of the thirteenth element
 * @param <E14> Type of the fourteenth element
 * @param <E15> Type of the fifteenth element
 * @param <E16> Type of the sixteenth element
 * @param <E17> Type of the seventeenth element
 * @param <E18> Type of the eighteenth element
 */
public interface Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> extends
    Tuple {

  /**
   * Returns a function that gets the first element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the first element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> FirstAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E1> getFirstGetter() {
    return Octodecuple::getFirst;
  }

  /**
   * Returns a function that gets the second element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the second element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> SecondAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E2> getSecondGetter() {
    return Octodecuple::getSecond;
  }

  /**
   * Returns a function that gets the third element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the third element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> ThirdAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E3> getThirdGetter() {
    return Octodecuple::getThird;
  }

  /**
   * Returns a function that gets the fourth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the fourth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> FourthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E4> getFourthGetter() {
    return Octodecuple::getFourth;
  }

  /**
   * Returns a function that gets the fifth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the fifth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> FifthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E5> getFifthGetter() {
    return Octodecuple::getFifth;
  }

  /**
   * Returns a function that gets the sixth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the sixth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> SixthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E6> getSixthGetter() {
    return Octodecuple::getSixth;
  }

  /**
   * Returns a function that gets the seventh element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the seventh element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> SeventhAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E7> getSeventhGetter() {
    return Octodecuple::getSeventh;
  }

  /**
   * Returns a function that gets the eighth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the eighth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> EighthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E8> getEighthGetter() {
    return Octodecuple::getEighth;
  }

  /**
   * Returns a function that gets the ninth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the ninth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> NinthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E9> getNinthGetter() {
    return Octodecuple::getNinth;
  }

  /**
   * Returns a function that gets the tenth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the tenth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> TenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E10> getTenthGetter() {
    return Octodecuple::getTenth;
  }

  /**
   * Returns a function that gets the eleventh element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the eleventh element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> EleventhAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E11> getEleventhGetter() {
    return Octodecuple::getEleventh;
  }

  /**
   * Returns a function that gets the twelfth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the twelfth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> TwelfthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E12> getTwelfthGetter() {
    return Octodecuple::getTwelfth;
  }

  /**
   * Returns a function that gets the thirteenth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the thirteenth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> ThirteenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E13> getThirteenthGetter() {
    return Octodecuple::getThirteenth;
  }

  /**
   * Returns a function that gets the fourteenth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the fourteenth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> FourteenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E14> getFourteenthGetter() {
    return Octodecuple::getFourteenth;
  }

  /**
   * Returns a function that gets the fifteenth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the fifteenth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> FifteenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E15> getFifteenthGetter() {
    return Octodecuple::getFifteenth;
  }

  /**
   * Returns a function that gets the sixteenth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the sixteenth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> SixteenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E16> getSixteenthGetter() {
    return Octodecuple::getSixteenth;
  }

  /**
   * Returns a function that gets the seventeenth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the seventeenth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> SeventeenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E17> getSeventeenthGetter() {
    return Octodecuple::getSeventeenth;
  }

  /**
   * Returns a function that gets the eighteenth element of an Octodecuple.
   *
   * @param <E1>  Type of the first element
   * @param <E2>  Type of the second element
   * @param <E3>  Type of the third element
   * @param <E4>  Type of the fourth element
   * @param <E5>  Type of the fifth element
   * @param <E6>  Type of the sixth element
   * @param <E7>  Type of the seventh element
   * @param <E8>  Type of the eighth element
   * @param <E9>  Type of the ninth element
   * @param <E10> Type of the tenth element
   * @param <E11> Type of the eleventh element
   * @param <E12> Type of the twelfth element
   * @param <E13> Type of the thirteenth element
   * @param <E14> Type of the fourteenth element
   * @param <E15> Type of the fifteenth element
   * @param <E16> Type of the sixteenth element
   * @param <E17> Type of the seventeenth element
   * @param <E18> Type of the eighteenth element
   * @return A function that extracts the eighteenth element from an Octodecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18> EighteenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>, E18> getEighteenthGetter() {
    return Octodecuple::getEighteenth;
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

  /**
   * Returns the sixteenth element of this tuple.
   *
   * @return the sixteenth element
   */
  E16 getSixteenth();

  /**
   * Returns the seventeenth element of this tuple.
   *
   * @return the seventeenth element
   */
  E17 getSeventeenth();

  /**
   * Returns the eighteenth element of this tuple.
   *
   * @return the eighteenth element
   */
  E18 getEighteenth();

  @Override
  default int size() {
    return 18;
  }

  /**
   * Returns the element at the specified index.
   *
   * @param index the index of the element to return (0-17)
   * @return the element at the specified index
   * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= 18)
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
      case 8 -> getNinth();
      case 9 -> getTenth();
      case 10 -> getEleventh();
      case 11 -> getTwelfth();
      case 12 -> getThirteenth();
      case 13 -> getFourteenth();
      case 14 -> getFifteenth();
      case 15 -> getSixteenth();
      case 16 -> getSeventeenth();
      case 17 -> getEighteenth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}