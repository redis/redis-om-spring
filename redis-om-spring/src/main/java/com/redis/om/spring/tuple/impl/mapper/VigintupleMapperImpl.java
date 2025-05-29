package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.tuple.Vigintuple;

/**
 * Implementation of a tuple mapper for 20-element tuples (Vigintuple).
 * This class provides functionality to map from a source type T to a Vigintuple containing twenty mapped values.
 *
 * @param <T>   the input type to map from
 * @param <T0>  the type of the first element in the resulting tuple
 * @param <T1>  the type of the second element in the resulting tuple
 * @param <T2>  the type of the third element in the resulting tuple
 * @param <T3>  the type of the fourth element in the resulting tuple
 * @param <T4>  the type of the fifth element in the resulting tuple
 * @param <T5>  the type of the sixth element in the resulting tuple
 * @param <T6>  the type of the seventh element in the resulting tuple
 * @param <T7>  the type of the eighth element in the resulting tuple
 * @param <T8>  the type of the ninth element in the resulting tuple
 * @param <T9>  the type of the tenth element in the resulting tuple
 * @param <T10> the type of the eleventh element in the resulting tuple
 * @param <T11> the type of the twelfth element in the resulting tuple
 * @param <T12> the type of the thirteenth element in the resulting tuple
 * @param <T13> the type of the fourteenth element in the resulting tuple
 * @param <T14> the type of the fifteenth element in the resulting tuple
 * @param <T15> the type of the sixteenth element in the resulting tuple
 * @param <T16> the type of the seventeenth element in the resulting tuple
 * @param <T17> the type of the eighteenth element in the resulting tuple
 * @param <T18> the type of the nineteenth element in the resulting tuple
 * @param <T19> the type of the twentieth element in the resulting tuple
 */
public final class VigintupleMapperImpl<T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>
    extends
    AbstractTupleMapper<T, Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>> {

  /**
   * Constructs a new VigintupleMapperImpl with the specified mapping functions.
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
   * @param m15 the function to map to the sixteenth element
   * @param m16 the function to map to the seventeenth element
   * @param m17 the function to map to the eighteenth element
   * @param m18 the function to map to the nineteenth element
   * @param m19 the function to map to the twentieth element
   */
  public VigintupleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3,
      Function<T, T4> m4, Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8,
      Function<T, T9> m9, Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13,
      Function<T, T14> m14, Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17, Function<T, T18> m18,
      Function<T, T19> m19) {
    super(20);
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
    set(15, m15);
    set(16, m16);
    set(17, m17);
    set(18, m18);
    set(19, m19);
  }

  @Override
  public Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> apply(
      T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t), getFourth().apply(t), getFifth()
        .apply(t), getSixth().apply(t), getSeventh().apply(t), getEighth().apply(t), getNinth().apply(t), getTenth()
            .apply(t), getEleventh().apply(t), getTwelfth().apply(t), getThirteenth().apply(t), getFourteenth().apply(
                t), getFifteenth().apply(t), getSixteenth().apply(t), getSeventeenth().apply(t), getEighteenth().apply(
                    t), getNineteenth().apply(t), getTwentieth().apply(t));
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

  /**
   * Gets the mapping function for the sixteenth element.
   *
   * @return the function that maps to the sixteenth element
   */
  public Function<T, T15> getSixteenth() {
    return getAndCast(15);
  }

  /**
   * Gets the mapping function for the seventeenth element.
   *
   * @return the function that maps to the seventeenth element
   */
  public Function<T, T16> getSeventeenth() {
    return getAndCast(16);
  }

  /**
   * Gets the mapping function for the eighteenth element.
   *
   * @return the function that maps to the eighteenth element
   */
  public Function<T, T17> getEighteenth() {
    return getAndCast(17);
  }

  /**
   * Gets the mapping function for the nineteenth element.
   *
   * @return the function that maps to the nineteenth element
   */
  public Function<T, T18> getNineteenth() {
    return getAndCast(18);
  }

  /**
   * Gets the mapping function for the twentieth element.
   *
   * @return the function that maps to the twentieth element
   */
  public Function<T, T19> getTwentieth() {
    return getAndCast(19);
  }
}