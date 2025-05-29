package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * Represents a tuple of sixteen elements.
 * <p>
 * This interface extends {@link Tuple} and provides access to sixteen typed elements
 * through getter methods and accessor functions. A sexdecuple (16-element tuple) is useful
 * for operations that need to return or process exactly sixteen related values together,
 * such as complex aggregation results, multi-field database projections, or composite
 * data structures in Redis OM Spring applications.
 * </p>
 * <p>
 * In the context of Redis OM Spring, sexdecuple tuples are commonly used for:
 * <ul>
 * <li>Complex aggregation results from RediSearch queries</li>
 * <li>Multi-field projections from Redis documents or hashes</li>
 * <li>Batch operations returning multiple related values</li>
 * <li>Entity streams that transform data into sixteen-component results</li>
 * </ul>
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
 */
public interface Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> extends Tuple {

  /**
   * Returns a function that extracts the first element from a Sexdecuple.
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
   * @return a function that extracts the first element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> FirstAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E1> getFirstGetter() {
    return Sexdecuple::getFirst;
  }

  /**
   * Returns a function that extracts the second element from a Sexdecuple.
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
   * @return a function that extracts the second element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> SecondAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E2> getSecondGetter() {
    return Sexdecuple::getSecond;
  }

  /**
   * Returns a function that extracts the third element from a Sexdecuple.
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
   * @return a function that extracts the third element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> ThirdAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E3> getThirdGetter() {
    return Sexdecuple::getThird;
  }

  /**
   * Returns a function that extracts the fourth element from a Sexdecuple.
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
   * @return a function that extracts the fourth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> FourthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E4> getFourthGetter() {
    return Sexdecuple::getFourth;
  }

  /**
   * Returns a function that extracts the fifth element from a Sexdecuple.
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
   * @return a function that extracts the fifth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> FifthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E5> getFifthGetter() {
    return Sexdecuple::getFifth;
  }

  /**
   * Returns a function that extracts the sixth element from a Sexdecuple.
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
   * @return a function that extracts the sixth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> SixthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E6> getSixthGetter() {
    return Sexdecuple::getSixth;
  }

  /**
   * Returns a function that extracts the seventh element from a Sexdecuple.
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
   * @return a function that extracts the seventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> SeventhAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E7> getSeventhGetter() {
    return Sexdecuple::getSeventh;
  }

  /**
   * Returns a function that extracts the eighth element from a Sexdecuple.
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
   * @return a function that extracts the eighth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> EighthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E8> getEighthGetter() {
    return Sexdecuple::getEighth;
  }

  /**
   * Returns a function that extracts the ninth element from a Sexdecuple.
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
   * @return a function that extracts the ninth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> NinthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E9> getNinthGetter() {
    return Sexdecuple::getNinth;
  }

  /**
   * Returns a function that extracts the tenth element from a Sexdecuple.
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
   * @return a function that extracts the tenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> TenthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E10> getTenthGetter() {
    return Sexdecuple::getTenth;
  }

  /**
   * Returns a function that extracts the eleventh element from a Sexdecuple.
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
   * @return a function that extracts the eleventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> EleventhAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E11> getEleventhGetter() {
    return Sexdecuple::getEleventh;
  }

  /**
   * Returns a function that extracts the twelfth element from a Sexdecuple.
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
   * @return a function that extracts the twelfth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> TwelfthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E12> getTwelfthGetter() {
    return Sexdecuple::getTwelfth;
  }

  /**
   * Returns a function that extracts the thirteenth element from a Sexdecuple.
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
   * @return a function that extracts the thirteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> ThirteenthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E13> getThirteenthGetter() {
    return Sexdecuple::getThirteenth;
  }

  /**
   * Returns a function that extracts the fourteenth element from a Sexdecuple.
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
   * @return a function that extracts the fourteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> FourteenthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E14> getFourteenthGetter() {
    return Sexdecuple::getFourteenth;
  }

  /**
   * Returns a function that extracts the fifteenth element from a Sexdecuple.
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
   * @return a function that extracts the fifteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> FifteenthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E15> getFifteenthGetter() {
    return Sexdecuple::getFifteenth;
  }

  /**
   * Returns a function that extracts the sixteenth element from a Sexdecuple.
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
   * @return a function that extracts the sixteenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16> SixteenthAccessor<Sexdecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16>, E16> getSixteenthGetter() {
    return Sexdecuple::getSixteenth;
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

  @Override
  default int size() {
    return 16;
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
      case 8 -> getNinth();
      case 9 -> getTenth();
      case 10 -> getEleventh();
      case 11 -> getTwelfth();
      case 12 -> getThirteenth();
      case 13 -> getFourteenth();
      case 14 -> getFifteenth();
      case 15 -> getSixteenth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}