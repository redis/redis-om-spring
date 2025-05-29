package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Nonuple;
import com.redis.om.spring.tuple.Tuples;

/**
 * A tuple mapper implementation that maps objects of type T to a Nonuple (9-element tuple).
 * This mapper applies nine transformation functions to convert a single input object into
 * a tuple containing nine elements of potentially different types.
 *
 * @param <T>  the input type to be mapped
 * @param <T0> the type of the first element in the resulting tuple
 * @param <T1> the type of the second element in the resulting tuple
 * @param <T2> the type of the third element in the resulting tuple
 * @param <T3> the type of the fourth element in the resulting tuple
 * @param <T4> the type of the fifth element in the resulting tuple
 * @param <T5> the type of the sixth element in the resulting tuple
 * @param <T6> the type of the seventh element in the resulting tuple
 * @param <T7> the type of the eighth element in the resulting tuple
 * @param <T8> the type of the ninth element in the resulting tuple
 */
public final class NonupleMapperImpl<T, T0, T1, T2, T3, T4, T5, T6, T7, T8> extends
    AbstractTupleMapper<T, Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8>> {

  /**
   * Constructs a new NonupleMapperImpl with the specified mapping functions.
   *
   * @param m0 the function to extract the first element from the input
   * @param m1 the function to extract the second element from the input
   * @param m2 the function to extract the third element from the input
   * @param m3 the function to extract the fourth element from the input
   * @param m4 the function to extract the fifth element from the input
   * @param m5 the function to extract the sixth element from the input
   * @param m6 the function to extract the seventh element from the input
   * @param m7 the function to extract the eighth element from the input
   * @param m8 the function to extract the ninth element from the input
   */
  public NonupleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3,
      Function<T, T4> m4, Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8) {
    super(9);
    set(0, m0);
    set(1, m1);
    set(2, m2);
    set(3, m3);
    set(4, m4);
    set(5, m5);
    set(6, m6);
    set(7, m7);
    set(8, m8);
  }

  @Override
  public Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t), getFourth().apply(t), getFifth()
        .apply(t), getSixth().apply(t), getSeventh().apply(t), getEighth().apply(t), getNinth().apply(t));
  }

  /**
   * Returns the function that extracts the first element of the tuple.
   *
   * @return the first mapping function
   */
  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  /**
   * Returns the function that extracts the second element of the tuple.
   *
   * @return the second mapping function
   */
  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }

  /**
   * Returns the function that extracts the third element of the tuple.
   *
   * @return the third mapping function
   */
  public Function<T, T2> getThird() {
    return getAndCast(2);
  }

  /**
   * Returns the function that extracts the fourth element of the tuple.
   *
   * @return the fourth mapping function
   */
  public Function<T, T3> getFourth() {
    return getAndCast(3);
  }

  /**
   * Returns the function that extracts the fifth element of the tuple.
   *
   * @return the fifth mapping function
   */
  public Function<T, T4> getFifth() {
    return getAndCast(4);
  }

  /**
   * Returns the function that extracts the sixth element of the tuple.
   *
   * @return the sixth mapping function
   */
  public Function<T, T5> getSixth() {
    return getAndCast(5);
  }

  /**
   * Returns the function that extracts the seventh element of the tuple.
   *
   * @return the seventh mapping function
   */
  public Function<T, T6> getSeventh() {
    return getAndCast(6);
  }

  /**
   * Returns the function that extracts the eighth element of the tuple.
   *
   * @return the eighth mapping function
   */
  public Function<T, T7> getEighth() {
    return getAndCast(7);
  }

  /**
   * Returns the function that extracts the ninth element of the tuple.
   *
   * @return the ninth mapping function
   */
  public Function<T, T8> getNinth() {
    return getAndCast(8);
  }
}