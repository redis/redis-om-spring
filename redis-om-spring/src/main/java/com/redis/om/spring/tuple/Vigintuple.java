package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * Represents a tuple of twenty elements.
 * <p>
 * This interface extends {@link Tuple} and provides access to twenty typed elements
 * through getter methods and accessor functions. A vigintuple (20-element tuple) is the
 * largest tuple implementation in Redis OM Spring and is useful for operations that need
 * to return or process exactly twenty related values together, such as extensive aggregation
 * results, comprehensive multi-field database projections, or complex composite data structures
 * in Redis OM Spring applications.
 * </p>
 * <p>
 * In the context of Redis OM Spring, vigintuple tuples are commonly used for:
 * <ul>
 * <li>Maximum complexity aggregation results from RediSearch queries</li>
 * <li>Comprehensive multi-field projections from Redis documents or hashes</li>
 * <li>Large batch operations returning multiple related values</li>
 * <li>Entity streams that transform data into twenty-component results</li>
 * <li>Complex reporting queries that aggregate data across many dimensions</li>
 * <li>Scientific or analytical applications requiring high-dimensional data structures</li>
 * </ul>
 * <p>
 * Note: This is the maximum tuple size supported by Redis OM Spring. For data structures
 * requiring more than 20 elements, consider using collections or custom domain objects.
 * </p>
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
 * @param <E16> the type of the sixteenth element
 * @param <E17> the type of the seventeenth element
 * @param <E18> the type of the eighteenth element
 * @param <E19> the type of the nineteenth element
 * @param <E20> the type of the twentieth element
 */
public interface Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>
    extends Tuple {

  /**
   * Returns a function that extracts the first element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the first element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> FirstAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E1> getFirstGetter() {
    return Vigintuple::getFirst;
  }

  /**
   * Returns a function that extracts the second element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the second element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> SecondAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E2> getSecondGetter() {
    return Vigintuple::getSecond;
  }

  /**
   * Returns a function that extracts the third element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the third element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> ThirdAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E3> getThirdGetter() {
    return Vigintuple::getThird;
  }

  /**
   * Returns a function that extracts the fourth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the fourth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> FourthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E4> getFourthGetter() {
    return Vigintuple::getFourth;
  }

  /**
   * Returns a function that extracts the fifth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the fifth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> FifthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E5> getFifthGetter() {
    return Vigintuple::getFifth;
  }

  /**
   * Returns a function that extracts the sixth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the sixth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> SixthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E6> getSixthGetter() {
    return Vigintuple::getSixth;
  }

  /**
   * Returns a function that extracts the seventh element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the seventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> SeventhAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E7> getSeventhGetter() {
    return Vigintuple::getSeventh;
  }

  /**
   * Returns a function that extracts the eighth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the eighth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> EighthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E8> getEighthGetter() {
    return Vigintuple::getEighth;
  }

  /**
   * Returns a function that extracts the ninth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the ninth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> NinthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E9> getNinthGetter() {
    return Vigintuple::getNinth;
  }

  /**
   * Returns a function that extracts the tenth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the tenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> TenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E10> getTenthGetter() {
    return Vigintuple::getTenth;
  }

  /**
   * Returns a function that extracts the eleventh element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the eleventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> EleventhAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E11> getEleventhGetter() {
    return Vigintuple::getEleventh;
  }

  /**
   * Returns a function that extracts the twelfth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the twelfth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> TwelfthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E12> getTwelfthGetter() {
    return Vigintuple::getTwelfth;
  }

  /**
   * Returns a function that extracts the thirteenth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the thirteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> ThirteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E13> getThirteenthGetter() {
    return Vigintuple::getThirteenth;
  }

  /**
   * Returns a function that extracts the fourteenth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the fourteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> FourteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E14> getFourteenthGetter() {
    return Vigintuple::getFourteenth;
  }

  /**
   * Returns a function that extracts the fifteenth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the fifteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> FifteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E15> getFifteenthGetter() {
    return Vigintuple::getFifteenth;
  }

  /**
   * Returns a function that extracts the sixteenth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the sixteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> SixteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E16> getSixteenthGetter() {
    return Vigintuple::getSixteenth;
  }

  /**
   * Returns a function that extracts the seventeenth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the seventeenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> SeventeenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E17> getSeventeenthGetter() {
    return Vigintuple::getSeventeenth;
  }

  /**
   * Returns a function that extracts the eighteenth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the eighteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> EighteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E18> getEighteenthGetter() {
    return Vigintuple::getEighteenth;
  }

  /**
   * Returns a function that extracts the nineteenth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the nineteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> NineteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E19> getNineteenthGetter() {
    return Vigintuple::getNineteenth;
  }

  /**
   * Returns a function that extracts the twentieth element from a Vigintuple.
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
   * @param <E16> the type of the sixteenth element
   * @param <E17> the type of the seventeenth element
   * @param <E18> the type of the eighteenth element
   * @param <E19> the type of the nineteenth element
   * @param <E20> the type of the twentieth element
   * @return a function that extracts the twentieth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> TwentiethAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E20> getTwentiethGetter() {
    return Vigintuple::getTwentieth;
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

  /**
   * Returns the nineteenth element of this tuple.
   *
   * @return the nineteenth element
   */
  E19 getNineteenth();

  /**
   * Returns the twentieth element of this tuple.
   *
   * @return the twentieth element
   */
  E20 getTwentieth();

  /**
   * Returns the number of elements in this tuple.
   * <p>
   * For a Vigintuple, this always returns 20.
   *
   * @return 20, the number of elements in this tuple
   */
  @Override
  default int size() {
    return 20;
  }

  /**
   * Returns the element at the specified position in this tuple.
   * <p>
   * The index is zero-based, so valid indices range from 0 to 19.
   *
   * @param index the index of the element to return (0-19)
   * @return the element at the specified position
   * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= 20)
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
      case 18 -> getNineteenth();
      case 19 -> getTwentieth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}