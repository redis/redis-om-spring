package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * A tuple interface representing an ordered collection of ten elements.
 * This interface extends the base Tuple interface and provides type-safe access
 * to each of the ten elements through dedicated getter methods.
 * 
 * <p>Decuple tuples are commonly used in Redis OM Spring for:
 * <ul>
 * <li>Query result projections with ten fields</li>
 * <li>Aggregation results containing ten values</li>
 * <li>Structured data representation with exactly ten components</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * Decuple<String, Integer, Double, Boolean, Date, Long, Float, Short, Byte, Character> result =
 *     Tuples.of("name", 42, 3.14, true, new Date(), 100L, 2.5f, (short)1, (byte)255, 'A');
 * 
 * String firstName = result.getFirst();
 * Integer age = result.getSecond();
 * }
 * </pre>
 *
 * @param <T0> the type of the first element
 * @param <T1> the type of the second element
 * @param <T2> the type of the third element
 * @param <T3> the type of the fourth element
 * @param <T4> the type of the fifth element
 * @param <T5> the type of the sixth element
 * @param <T6> the type of the seventh element
 * @param <T7> the type of the eighth element
 * @param <T8> the type of the ninth element
 * @param <T9> the type of the tenth element
 * 
 * @author Redis OM Spring Development Team
 * @since 0.8.0
 */
public interface Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> extends Tuple {

  /**
   * Returns a function accessor for the first element of the decuple.
   * This method provides a functional interface for accessing the first element,
   * useful in functional programming contexts and stream operations.
   *
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @return a FirstAccessor function for accessing the first element
   */
  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> FirstAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T0> getFirstGetter() {
    return Decuple::getFirst;
  }

  /**
   * Returns a function accessor for the second element of the decuple.
   *
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @return a SecondAccessor function for accessing the second element
   */
  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> SecondAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T1> getSecondGetter() {
    return Decuple::getSecond;
  }

  /**
   * Returns a function accessor for the third element of the decuple.
   *
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @return a ThirdAccessor function for accessing the third element
   */
  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> ThirdAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T2> getThirdGetter() {
    return Decuple::getThird;
  }

  /**
   * Returns a function accessor for the fourth element of the decuple.
   *
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @return a FourthAccessor function for accessing the fourth element
   */
  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> FourthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T3> getFourthGetter() {
    return Decuple::getFourth;
  }

  /**
   * Returns a function accessor for the fifth element of the decuple.
   *
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @return a FifthAccessor function for accessing the fifth element
   */
  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> FifthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T4> getFifthGetter() {
    return Decuple::getFifth;
  }

  /**
   * Returns a function accessor for the sixth element of the decuple.
   *
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @return a SixthAccessor function for accessing the sixth element
   */
  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> SixthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T5> getSixthGetter() {
    return Decuple::getSixth;
  }

  /**
   * Returns a function accessor for the seventh element of the decuple.
   *
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @return a SeventhAccessor function for accessing the seventh element
   */
  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> SeventhAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T6> getSeventhGetter() {
    return Decuple::getSeventh;
  }

  /**
   * Returns a function accessor for the eighth element of the decuple.
   *
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @return an EighthAccessor function for accessing the eighth element
   */
  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> EighthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T7> getEighthGetter() {
    return Decuple::getEighth;
  }

  /**
   * Returns a function accessor for the ninth element of the decuple.
   *
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @return a NinthAccessor function for accessing the ninth element
   */
  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> NinthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T8> getNinthGetter() {
    return Decuple::getNinth;
  }

  /**
   * Returns a function accessor for the tenth element of the decuple.
   *
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @return a TenthAccessor function for accessing the tenth element
   */
  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> TenthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T9> getTenthGetter() {
    return Decuple::getTenth;
  }

  /**
   * Gets the first element of the decuple.
   *
   * @return the first element
   */
  T0 getFirst();

  /**
   * Gets the second element of the decuple.
   *
   * @return the second element
   */
  T1 getSecond();

  /**
   * Gets the third element of the decuple.
   *
   * @return the third element
   */
  T2 getThird();

  /**
   * Gets the fourth element of the decuple.
   *
   * @return the fourth element
   */
  T3 getFourth();

  /**
   * Gets the fifth element of the decuple.
   *
   * @return the fifth element
   */
  T4 getFifth();

  /**
   * Gets the sixth element of the decuple.
   *
   * @return the sixth element
   */
  T5 getSixth();

  /**
   * Gets the seventh element of the decuple.
   *
   * @return the seventh element
   */
  T6 getSeventh();

  /**
   * Gets the eighth element of the decuple.
   *
   * @return the eighth element
   */
  T7 getEighth();

  /**
   * Gets the ninth element of the decuple.
   *
   * @return the ninth element
   */
  T8 getNinth();

  /**
   * Gets the tenth element of the decuple.
   *
   * @return the tenth element
   */
  T9 getTenth();

  /**
   * Returns the number of elements in this decuple, which is always 10.
   *
   * @return the size of the decuple (always 10)
   */
  @Override
  default int size() {
    return 10;
  }

  /**
   * Gets an element at the specified index position.
   * This method provides array-like access to tuple elements using zero-based indexing.
   *
   * @param index the zero-based index of the element to retrieve (0-9)
   * @return the element at the specified index
   * @throws IndexOutOfBoundsException if the index is not between 0 and 9 (inclusive)
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
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}