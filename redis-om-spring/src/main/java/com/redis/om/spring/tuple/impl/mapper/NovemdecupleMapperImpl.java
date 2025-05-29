package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Novemdecuple;
import com.redis.om.spring.tuple.Tuples;

/**
 * Implementation of a mapper that transforms objects of type {@code T} into {@link Novemdecuple} instances.
 * <p>
 * This mapper uses nineteen transformation functions to extract values from the source object
 * and create a tuple containing those values.
 * </p>
 *
 * @param <T>   the source type to be mapped from
 * @param <T0>  the type of the first tuple element
 * @param <T1>  the type of the second tuple element
 * @param <T2>  the type of the third tuple element
 * @param <T3>  the type of the fourth tuple element
 * @param <T4>  the type of the fifth tuple element
 * @param <T5>  the type of the sixth tuple element
 * @param <T6>  the type of the seventh tuple element
 * @param <T7>  the type of the eighth tuple element
 * @param <T8>  the type of the ninth tuple element
 * @param <T9>  the type of the tenth tuple element
 * @param <T10> the type of the eleventh tuple element
 * @param <T11> the type of the twelfth tuple element
 * @param <T12> the type of the thirteenth tuple element
 * @param <T13> the type of the fourteenth tuple element
 * @param <T14> the type of the fifteenth tuple element
 * @param <T15> the type of the sixteenth tuple element
 * @param <T16> the type of the seventeenth tuple element
 * @param <T17> the type of the eighteenth tuple element
 * @param <T18> the type of the nineteenth tuple element
 */
public final class NovemdecupleMapperImpl<T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>
    extends
    AbstractTupleMapper<T, Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>> {

  /**
   * Constructs a new NovemdecupleMapperImpl with the specified mapping functions.
   *
   * @param m0  function to extract the first element from the source object
   * @param m1  function to extract the second element from the source object
   * @param m2  function to extract the third element from the source object
   * @param m3  function to extract the fourth element from the source object
   * @param m4  function to extract the fifth element from the source object
   * @param m5  function to extract the sixth element from the source object
   * @param m6  function to extract the seventh element from the source object
   * @param m7  function to extract the eighth element from the source object
   * @param m8  function to extract the ninth element from the source object
   * @param m9  function to extract the tenth element from the source object
   * @param m10 function to extract the eleventh element from the source object
   * @param m11 function to extract the twelfth element from the source object
   * @param m12 function to extract the thirteenth element from the source object
   * @param m13 function to extract the fourteenth element from the source object
   * @param m14 function to extract the fifteenth element from the source object
   * @param m15 function to extract the sixteenth element from the source object
   * @param m16 function to extract the seventeenth element from the source object
   * @param m17 function to extract the eighteenth element from the source object
   * @param m18 function to extract the nineteenth element from the source object
   */
  public NovemdecupleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3,
      Function<T, T4> m4, Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8,
      Function<T, T9> m9, Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13,
      Function<T, T14> m14, Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17, Function<T, T18> m18) {
    super(19);
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
  }

  /**
   * Applies the mapping functions to the input object and creates a Novemdecuple.
   *
   * @param t the input object to be mapped
   * @return a Novemdecuple containing the mapped values
   */
  @Override
  public Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t), getFourth().apply(t), getFifth()
        .apply(t), getSixth().apply(t), getSeventh().apply(t), getEighth().apply(t), getNinth().apply(t), getTenth()
            .apply(t), getEleventh().apply(t), getTwelfth().apply(t), getThirteenth().apply(t), getFourteenth().apply(
                t), getFifteenth().apply(t), getSixteenth().apply(t), getSeventeenth().apply(t), getEighteenth().apply(
                    t), getNineteenth().apply(t));
  }

  /**
   * Returns the mapping function for the first element.
   *
   * @return the mapping function for the first element
   */
  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  /**
   * Returns the mapping function for the second element.
   *
   * @return the mapping function for the second element
   */
  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }

  /**
   * Returns the mapping function for the third element.
   *
   * @return the mapping function for the third element
   */
  public Function<T, T2> getThird() {
    return getAndCast(2);
  }

  /**
   * Returns the mapping function for the fourth element.
   *
   * @return the mapping function for the fourth element
   */
  public Function<T, T3> getFourth() {
    return getAndCast(3);
  }

  /**
   * Returns the mapping function for the fifth element.
   *
   * @return the mapping function for the fifth element
   */
  public Function<T, T4> getFifth() {
    return getAndCast(4);
  }

  /**
   * Returns the mapping function for the sixth element.
   *
   * @return the mapping function for the sixth element
   */
  public Function<T, T5> getSixth() {
    return getAndCast(5);
  }

  /**
   * Returns the mapping function for the seventh element.
   *
   * @return the mapping function for the seventh element
   */
  public Function<T, T6> getSeventh() {
    return getAndCast(6);
  }

  /**
   * Returns the mapping function for the eighth element.
   *
   * @return the mapping function for the eighth element
   */
  public Function<T, T7> getEighth() {
    return getAndCast(7);
  }

  /**
   * Returns the mapping function for the ninth element.
   *
   * @return the mapping function for the ninth element
   */
  public Function<T, T8> getNinth() {
    return getAndCast(8);
  }

  /**
   * Returns the mapping function for the tenth element.
   *
   * @return the mapping function for the tenth element
   */
  public Function<T, T9> getTenth() {
    return getAndCast(9);
  }

  /**
   * Returns the mapping function for the eleventh element.
   *
   * @return the mapping function for the eleventh element
   */
  public Function<T, T10> getEleventh() {
    return getAndCast(10);
  }

  /**
   * Returns the mapping function for the twelfth element.
   *
   * @return the mapping function for the twelfth element
   */
  public Function<T, T11> getTwelfth() {
    return getAndCast(11);
  }

  /**
   * Returns the mapping function for the thirteenth element.
   *
   * @return the mapping function for the thirteenth element
   */
  public Function<T, T12> getThirteenth() {
    return getAndCast(12);
  }

  /**
   * Returns the mapping function for the fourteenth element.
   *
   * @return the mapping function for the fourteenth element
   */
  public Function<T, T13> getFourteenth() {
    return getAndCast(13);
  }

  /**
   * Returns the mapping function for the fifteenth element.
   *
   * @return the mapping function for the fifteenth element
   */
  public Function<T, T14> getFifteenth() {
    return getAndCast(14);
  }

  /**
   * Returns the mapping function for the sixteenth element.
   *
   * @return the mapping function for the sixteenth element
   */
  public Function<T, T15> getSixteenth() {
    return getAndCast(15);
  }

  /**
   * Returns the mapping function for the seventeenth element.
   *
   * @return the mapping function for the seventeenth element
   */
  public Function<T, T16> getSeventeenth() {
    return getAndCast(16);
  }

  /**
   * Returns the mapping function for the eighteenth element.
   *
   * @return the mapping function for the eighteenth element
   */
  public Function<T, T17> getEighteenth() {
    return getAndCast(17);
  }

  /**
   * Returns the mapping function for the nineteenth element.
   *
   * @return the mapping function for the nineteenth element
   */
  public Function<T, T18> getNineteenth() {
    return getAndCast(18);
  }
}