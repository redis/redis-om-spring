package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Quindecuple;
import com.redis.om.spring.tuple.Tuples;

/**
 * Implementation of a tuple mapper for 15-element tuples (Quindecuple).
 * This class provides functionality to map from a source type T to a Quindecuple containing fifteen mapped values.
 *
 * @param <T>   the input type to map from
 * @param <T0>  the type of the first element in the resulting quindecuple
 * @param <T1>  the type of the second element in the resulting quindecuple
 * @param <T2>  the type of the third element in the resulting quindecuple
 * @param <T3>  the type of the fourth element in the resulting quindecuple
 * @param <T4>  the type of the fifth element in the resulting quindecuple
 * @param <T5>  the type of the sixth element in the resulting quindecuple
 * @param <T6>  the type of the seventh element in the resulting quindecuple
 * @param <T7>  the type of the eighth element in the resulting quindecuple
 * @param <T8>  the type of the ninth element in the resulting quindecuple
 * @param <T9>  the type of the tenth element in the resulting quindecuple
 * @param <T10> the type of the eleventh element in the resulting quindecuple
 * @param <T11> the type of the twelfth element in the resulting quindecuple
 * @param <T12> the type of the thirteenth element in the resulting quindecuple
 * @param <T13> the type of the fourteenth element in the resulting quindecuple
 * @param <T14> the type of the fifteenth element in the resulting quindecuple
 */
public final class QuindecupleMapperImpl<T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> extends
    AbstractTupleMapper<T, Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> {

  /**
   * Constructs a new QuindecupleMapperImpl with the specified mapping functions.
   *
   * @param m0  the function to map to the first element
   * @param m1  the function to map to the second element
   * @param m2  the function to map to the third element
   * @param m3  the function to map to the fourth element
   * @param m4  the function to map to the fifth element
   * @param m5  the function to map to the sixth element
   * @param m6  the function to map to the seventh element
   * @param m7  the function to map to the eighth element
   * @param m8  the function to map to the ninth element
   * @param m9  the function to map to the tenth element
   * @param m10 the function to map to the eleventh element
   * @param m11 the function to map to the twelfth element
   * @param m12 the function to map to the thirteenth element
   * @param m13 the function to map to the fourteenth element
   * @param m14 the function to map to the fifteenth element
   */
  public QuindecupleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3,
      Function<T, T4> m4, Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8,
      Function<T, T9> m9, Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13,
      Function<T, T14> m14) {
    super(15);
    set(0, m0);
    set(1, m1);
    set(2, m2);
    set(3, m3);
    set(4, m4);
    set(5, m5);
    set(6, m6);
    set(7, m7);
    set(8, m8);
    set(9, m9);
    set(10, m10);
    set(11, m11);
    set(12, m12);
    set(13, m13);
    set(14, m14);
  }

  @Override
  public Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t), getFourth().apply(t), getFifth()
        .apply(t), getSixth().apply(t), getSeventh().apply(t), getEighth().apply(t), getNinth().apply(t), getTenth()
            .apply(t), getEleventh().apply(t), getTwelfth().apply(t), getThirteenth().apply(t), getFourteenth().apply(
                t), getFifteenth().apply(t));
  }

  /**
   * Gets the mapping function for the first element.
   *
   * @return the function that maps to the first element
   */
  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  /**
   * Gets the mapping function for the second element.
   *
   * @return the function that maps to the second element
   */
  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }

  /**
   * Gets the mapping function for the third element.
   *
   * @return the function that maps to the third element
   */
  public Function<T, T2> getThird() {
    return getAndCast(2);
  }

  /**
   * Gets the mapping function for the fourth element.
   *
   * @return the function that maps to the fourth element
   */
  public Function<T, T3> getFourth() {
    return getAndCast(3);
  }

  /**
   * Gets the mapping function for the fifth element.
   *
   * @return the function that maps to the fifth element
   */
  public Function<T, T4> getFifth() {
    return getAndCast(4);
  }

  /**
   * Gets the mapping function for the sixth element.
   *
   * @return the function that maps to the sixth element
   */
  public Function<T, T5> getSixth() {
    return getAndCast(5);
  }

  /**
   * Gets the mapping function for the seventh element.
   *
   * @return the function that maps to the seventh element
   */
  public Function<T, T6> getSeventh() {
    return getAndCast(6);
  }

  /**
   * Gets the mapping function for the eighth element.
   *
   * @return the function that maps to the eighth element
   */
  public Function<T, T7> getEighth() {
    return getAndCast(7);
  }

  /**
   * Gets the mapping function for the ninth element.
   *
   * @return the function that maps to the ninth element
   */
  public Function<T, T8> getNinth() {
    return getAndCast(8);
  }

  /**
   * Gets the mapping function for the tenth element.
   *
   * @return the function that maps to the tenth element
   */
  public Function<T, T9> getTenth() {
    return getAndCast(9);
  }

  /**
   * Gets the mapping function for the eleventh element.
   *
   * @return the function that maps to the eleventh element
   */
  public Function<T, T10> getEleventh() {
    return getAndCast(10);
  }

  /**
   * Gets the mapping function for the twelfth element.
   *
   * @return the function that maps to the twelfth element
   */
  public Function<T, T11> getTwelfth() {
    return getAndCast(11);
  }

  /**
   * Gets the mapping function for the thirteenth element.
   *
   * @return the function that maps to the thirteenth element
   */
  public Function<T, T12> getThirteenth() {
    return getAndCast(12);
  }

  /**
   * Gets the mapping function for the fourteenth element.
   *
   * @return the function that maps to the fourteenth element
   */
  public Function<T, T13> getFourteenth() {
    return getAndCast(13);
  }

  /**
   * Gets the mapping function for the fifteenth element.
   *
   * @return the function that maps to the fifteenth element
   */
  public Function<T, T14> getFifteenth() {
    return getAndCast(14);
  }
}