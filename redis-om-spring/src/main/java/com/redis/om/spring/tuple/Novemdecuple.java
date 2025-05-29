package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * A tuple of degree 19 that holds nineteen elements.
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
 * @param <E19> Type of the nineteenth element
 */
public interface Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>
    extends Tuple {

  /**
   * Returns a function that gets the first element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the first element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> FirstAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E1> getFirstGetter() {
    return Novemdecuple::getFirst;
  }

  /**
   * Returns a function that gets the second element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the second element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> SecondAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E2> getSecondGetter() {
    return Novemdecuple::getSecond;
  }

  /**
   * Returns a function that gets the third element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the third element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> ThirdAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E3> getThirdGetter() {
    return Novemdecuple::getThird;
  }

  /**
   * Returns a function that gets the fourth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the fourth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> FourthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E4> getFourthGetter() {
    return Novemdecuple::getFourth;
  }

  /**
   * Returns a function that gets the fifth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the fifth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> FifthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E5> getFifthGetter() {
    return Novemdecuple::getFifth;
  }

  /**
   * Returns a function that gets the sixth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the sixth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> SixthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E6> getSixthGetter() {
    return Novemdecuple::getSixth;
  }

  /**
   * Returns a function that gets the seventh element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the seventh element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> SeventhAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E7> getSeventhGetter() {
    return Novemdecuple::getSeventh;
  }

  /**
   * Returns a function that gets the eighth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the eighth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> EighthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E8> getEighthGetter() {
    return Novemdecuple::getEighth;
  }

  /**
   * Returns a function that gets the ninth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the ninth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> NinthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E9> getNinthGetter() {
    return Novemdecuple::getNinth;
  }

  /**
   * Returns a function that gets the tenth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the tenth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> TenthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E10> getTenthGetter() {
    return Novemdecuple::getTenth;
  }

  /**
   * Returns a function that gets the eleventh element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the eleventh element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> EleventhAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E11> getEleventhGetter() {
    return Novemdecuple::getEleventh;
  }

  /**
   * Returns a function that gets the twelfth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the twelfth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> TwelfthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E12> getTwelfthGetter() {
    return Novemdecuple::getTwelfth;
  }

  /**
   * Returns a function that gets the thirteenth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the thirteenth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> ThirteenthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E13> getThirteenthGetter() {
    return Novemdecuple::getThirteenth;
  }

  /**
   * Returns a function that gets the fourteenth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the fourteenth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> FourteenthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E14> getFourteenthGetter() {
    return Novemdecuple::getFourteenth;
  }

  /**
   * Returns a function that gets the fifteenth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the fifteenth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> FifteenthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E15> getFifteenthGetter() {
    return Novemdecuple::getFifteenth;
  }

  /**
   * Returns a function that gets the sixteenth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the sixteenth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> SixteenthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E16> getSixteenthGetter() {
    return Novemdecuple::getSixteenth;
  }

  /**
   * Returns a function that gets the seventeenth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the seventeenth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> SeventeenthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E17> getSeventeenthGetter() {
    return Novemdecuple::getSeventeenth;
  }

  /**
   * Returns a function that gets the eighteenth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the eighteenth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> EighteenthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E18> getEighteenthGetter() {
    return Novemdecuple::getEighteenth;
  }

  /**
   * Returns a function that gets the nineteenth element of a Novemdecuple.
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
   * @param <E19> Type of the nineteenth element
   * @return A function that gets the nineteenth element of a Novemdecuple
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19> NineteenthAccessor<Novemdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19>, E19> getNineteenthGetter() {
    return Novemdecuple::getNineteenth;
  }

  /**
   * Returns the first element of this tuple.
   *
   * @return the first element of this tuple
   */
  E1 getFirst();

  /**
   * Returns the second element of this tuple.
   *
   * @return the second element of this tuple
   */
  E2 getSecond();

  /**
   * Returns the third element of this tuple.
   *
   * @return the third element of this tuple
   */
  E3 getThird();

  /**
   * Returns the fourth element of this tuple.
   *
   * @return the fourth element of this tuple
   */
  E4 getFourth();

  /**
   * Returns the fifth element of this tuple.
   *
   * @return the fifth element of this tuple
   */
  E5 getFifth();

  /**
   * Returns the sixth element of this tuple.
   *
   * @return the sixth element of this tuple
   */
  E6 getSixth();

  /**
   * Returns the seventh element of this tuple.
   *
   * @return the seventh element of this tuple
   */
  E7 getSeventh();

  /**
   * Returns the eighth element of this tuple.
   *
   * @return the eighth element of this tuple
   */
  E8 getEighth();

  /**
   * Returns the ninth element of this tuple.
   *
   * @return the ninth element of this tuple
   */
  E9 getNinth();

  /**
   * Returns the tenth element of this tuple.
   *
   * @return the tenth element of this tuple
   */
  E10 getTenth();

  /**
   * Returns the eleventh element of this tuple.
   *
   * @return the eleventh element of this tuple
   */
  E11 getEleventh();

  /**
   * Returns the twelfth element of this tuple.
   *
   * @return the twelfth element of this tuple
   */
  E12 getTwelfth();

  /**
   * Returns the thirteenth element of this tuple.
   *
   * @return the thirteenth element of this tuple
   */
  E13 getThirteenth();

  /**
   * Returns the fourteenth element of this tuple.
   *
   * @return the fourteenth element of this tuple
   */
  E14 getFourteenth();

  /**
   * Returns the fifteenth element of this tuple.
   *
   * @return the fifteenth element of this tuple
   */
  E15 getFifteenth();

  /**
   * Returns the sixteenth element of this tuple.
   *
   * @return the sixteenth element of this tuple
   */
  E16 getSixteenth();

  /**
   * Returns the seventeenth element of this tuple.
   *
   * @return the seventeenth element of this tuple
   */
  E17 getSeventeenth();

  /**
   * Returns the eighteenth element of this tuple.
   *
   * @return the eighteenth element of this tuple
   */
  E18 getEighteenth();

  /**
   * Returns the nineteenth element of this tuple.
   *
   * @return the nineteenth element of this tuple
   */
  E19 getNineteenth();

  @Override
  default int size() {
    return 19;
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
      case 15 -> getSixteenth();
      case 16 -> getSeventeenth();
      case 17 -> getEighteenth();
      case 18 -> getNineteenth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}