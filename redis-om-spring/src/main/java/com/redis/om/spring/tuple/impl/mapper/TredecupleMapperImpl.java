package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Tredecuple;
import com.redis.om.spring.tuple.Tuples;

/**
 * A mapper implementation for creating Tredecuples from a source object.
 *
 * @param <T>   the type of the source object
 * @param <T0>  the type of the first element
 * @param <T1>  the type of the second element
 * @param <T2>  the type of the third element
 * @param <T3>  the type of the fourth element
 * @param <T4>  the type of the fifth element
 * @param <T5>  the type of the sixth element
 * @param <T6>  the type of the seventh element
 * @param <T7>  the type of the eighth element
 * @param <T8>  the type of the ninth element
 * @param <T9>  the type of the tenth element
 * @param <T10> the type of the eleventh element
 * @param <T11> the type of the twelfth element
 * @param <T12> the type of the thirteenth element
 */
public final class TredecupleMapperImpl<T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> extends
    AbstractTupleMapper<T, Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> {

  /**
   * Constructs a new TredecupleMapperImpl with the specified mapping functions.
   *
   * @param m0  the function to extract the first element
   * @param m1  the function to extract the second element
   * @param m2  the function to extract the third element
   * @param m3  the function to extract the fourth element
   * @param m4  the function to extract the fifth element
   * @param m5  the function to extract the sixth element
   * @param m6  the function to extract the seventh element
   * @param m7  the function to extract the eighth element
   * @param m8  the function to extract the ninth element
   * @param m9  the function to extract the tenth element
   * @param m10 the function to extract the eleventh element
   * @param m11 the function to extract the twelfth element
   * @param m12 the function to extract the thirteenth element
   */
  public TredecupleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3,
      Function<T, T4> m4, Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8,
      Function<T, T9> m9, Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12) {
    super(13);
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
  }

  @Override
  public Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t), getFourth().apply(t), getFifth()
        .apply(t), getSixth().apply(t), getSeventh().apply(t), getEighth().apply(t), getNinth().apply(t), getTenth()
            .apply(t), getEleventh().apply(t), getTwelfth().apply(t), getThirteenth().apply(t));
  }

  /**
   * Returns the function that extracts the first element.
   *
   * @return the function that extracts the first element
   */
  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  /**
   * Returns the function that extracts the second element.
   *
   * @return the function that extracts the second element
   */
  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }

  /**
   * Returns the function that extracts the third element.
   *
   * @return the function that extracts the third element
   */
  public Function<T, T2> getThird() {
    return getAndCast(2);
  }

  /**
   * Returns the function that extracts the fourth element.
   *
   * @return the function that extracts the fourth element
   */
  public Function<T, T3> getFourth() {
    return getAndCast(3);
  }

  /**
   * Returns the function that extracts the fifth element.
   *
   * @return the function that extracts the fifth element
   */
  public Function<T, T4> getFifth() {
    return getAndCast(4);
  }

  /**
   * Returns the function that extracts the sixth element.
   *
   * @return the function that extracts the sixth element
   */
  public Function<T, T5> getSixth() {
    return getAndCast(5);
  }

  /**
   * Returns the function that extracts the seventh element.
   *
   * @return the function that extracts the seventh element
   */
  public Function<T, T6> getSeventh() {
    return getAndCast(6);
  }

  /**
   * Returns the function that extracts the eighth element.
   *
   * @return the function that extracts the eighth element
   */
  public Function<T, T7> getEighth() {
    return getAndCast(7);
  }

  /**
   * Returns the function that extracts the ninth element.
   *
   * @return the function that extracts the ninth element
   */
  public Function<T, T8> getNinth() {
    return getAndCast(8);
  }

  /**
   * Returns the function that extracts the tenth element.
   *
   * @return the function that extracts the tenth element
   */
  public Function<T, T9> getTenth() {
    return getAndCast(9);
  }

  /**
   * Returns the function that extracts the eleventh element.
   *
   * @return the function that extracts the eleventh element
   */
  public Function<T, T10> getEleventh() {
    return getAndCast(10);
  }

  /**
   * Returns the function that extracts the twelfth element.
   *
   * @return the function that extracts the twelfth element
   */
  public Function<T, T11> getTwelfth() {
    return getAndCast(11);
  }

  /**
   * Returns the function that extracts the thirteenth element.
   *
   * @return the function that extracts the thirteenth element
   */
  public Function<T, T12> getThirteenth() {
    return getAndCast(12);
  }
}