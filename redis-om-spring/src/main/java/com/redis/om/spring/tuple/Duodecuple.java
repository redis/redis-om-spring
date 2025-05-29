package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * A tuple interface representing an ordered collection of twelve elements.
 * This interface extends the base Tuple interface and provides type-safe access
 * to each of the twelve elements through dedicated getter methods.
 * 
 * <p>Duodecuple tuples are commonly used in Redis OM Spring for:
 * <ul>
 * <li>Complex query result projections with twelve fields</li>
 * <li>Large aggregation results containing twelve values</li>
 * <li>Structured data representation with exactly twelve components</li>
 * <li>Data transformation operations requiring twelve distinct elements</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * Duodecuple<String, Integer, Double, Boolean, Date, Long, Float, Short, Byte, Character, BigDecimal, UUID> result =
 *     Tuples.of("name", 42, 3.14, true, new Date(), 100L, 2.5f, (short)1, (byte)255, 'A', new BigDecimal("99.99"), UUID.randomUUID());
 * 
 * String firstName = result.getFirst();
 * Integer age = result.getSecond();
 * UUID uniqueId = result.getTwelfth();
 * }
 * </pre>
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
 * 
 * @author Redis OM Spring Development Team
 * @since 0.8.0
 */
public interface Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> extends Tuple {

  /**
   * Returns a function accessor for the first element of the duodecuple.
   * This method provides a functional interface for accessing the first element,
   * useful in functional programming contexts and stream operations.
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
   * @return a FirstAccessor function for accessing the first element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> FirstAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E1> getFirstGetter() {
    return Duodecuple::getFirst;
  }

  /**
   * Returns a function accessor for the second element of the duodecuple.
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
   * @return a SecondAccessor function for accessing the second element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> SecondAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E2> getSecondGetter() {
    return Duodecuple::getSecond;
  }

  /**
   * Returns a function accessor for the third element of the duodecuple.
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
   * @return a ThirdAccessor function for accessing the third element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> ThirdAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E3> getThirdGetter() {
    return Duodecuple::getThird;
  }

  /**
   * Returns a function accessor for the fourth element of the duodecuple.
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
   * @return a FourthAccessor function for accessing the fourth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> FourthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E4> getFourthGetter() {
    return Duodecuple::getFourth;
  }

  /**
   * Returns a function accessor for the fifth element of the duodecuple.
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
   * @return a FifthAccessor function for accessing the fifth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> FifthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E5> getFifthGetter() {
    return Duodecuple::getFifth;
  }

  /**
   * Returns a function accessor for the sixth element of the duodecuple.
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
   * @return a SixthAccessor function for accessing the sixth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> SixthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E6> getSixthGetter() {
    return Duodecuple::getSixth;
  }

  /**
   * Returns a function accessor for the seventh element of the duodecuple.
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
   * @return a SeventhAccessor function for accessing the seventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> SeventhAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E7> getSeventhGetter() {
    return Duodecuple::getSeventh;
  }

  /**
   * Returns a function accessor for the eighth element of the duodecuple.
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
   * @return an EighthAccessor function for accessing the eighth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> EighthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E8> getEighthGetter() {
    return Duodecuple::getEighth;
  }

  /**
   * Returns a function accessor for the ninth element of the duodecuple.
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
   * @return a NinthAccessor function for accessing the ninth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> NinthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E9> getNinthGetter() {
    return Duodecuple::getNinth;
  }

  /**
   * Returns a function accessor for the tenth element of the duodecuple.
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
   * @return a TenthAccessor function for accessing the tenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> TenthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E10> getTenthGetter() {
    return Duodecuple::getTenth;
  }

  /**
   * Returns a function accessor for the eleventh element of the duodecuple.
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
   * @return an EleventhAccessor function for accessing the eleventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> EleventhAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E11> getEleventhGetter() {
    return Duodecuple::getEleventh;
  }

  /**
   * Returns a function accessor for the twelfth element of the duodecuple.
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
   * @return a TwelfthAccessor function for accessing the twelfth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> TwelfthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E12> getTwelfthGetter() {
    return Duodecuple::getTwelfth;
  }

  /**
   * Gets the first element of the duodecuple.
   *
   * @return the first element
   */
  E1 getFirst();

  /**
   * Gets the second element of the duodecuple.
   *
   * @return the second element
   */
  E2 getSecond();

  /**
   * Gets the third element of the duodecuple.
   *
   * @return the third element
   */
  E3 getThird();

  /**
   * Gets the fourth element of the duodecuple.
   *
   * @return the fourth element
   */
  E4 getFourth();

  /**
   * Gets the fifth element of the duodecuple.
   *
   * @return the fifth element
   */
  E5 getFifth();

  /**
   * Gets the sixth element of the duodecuple.
   *
   * @return the sixth element
   */
  E6 getSixth();

  /**
   * Gets the seventh element of the duodecuple.
   *
   * @return the seventh element
   */
  E7 getSeventh();

  /**
   * Gets the eighth element of the duodecuple.
   *
   * @return the eighth element
   */
  E8 getEighth();

  /**
   * Gets the ninth element of the duodecuple.
   *
   * @return the ninth element
   */
  E9 getNinth();

  /**
   * Gets the tenth element of the duodecuple.
   *
   * @return the tenth element
   */
  E10 getTenth();

  /**
   * Gets the eleventh element of the duodecuple.
   *
   * @return the eleventh element
   */
  E11 getEleventh();

  /**
   * Gets the twelfth element of the duodecuple.
   *
   * @return the twelfth element
   */
  E12 getTwelfth();

  /**
   * Returns the number of elements in this duodecuple, which is always 12.
   *
   * @return the size of the duodecuple (always 12)
   */
  @Override
  default int size() {
    return 12;
  }

  /**
   * Gets an element at the specified index position.
   * This method provides array-like access to tuple elements using zero-based indexing.
   *
   * @param index the zero-based index of the element to retrieve (0-11)
   * @return the element at the specified index
   * @throws IndexOutOfBoundsException if the index is not between 0 and 11 (inclusive)
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
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}