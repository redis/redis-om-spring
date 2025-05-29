package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Quad;
import com.redis.om.spring.tuple.Tuples;

/**
 * Implementation of a tuple mapper for 4-element tuples (Quad).
 * This class provides functionality to map from a source type T to a Quad containing four mapped values.
 *
 * @param <T>  the input type to map from
 * @param <T0> the type of the first element in the resulting quad
 * @param <T1> the type of the second element in the resulting quad
 * @param <T2> the type of the third element in the resulting quad
 * @param <T3> the type of the fourth element in the resulting quad
 */
public final class QuadMapperImpl<T, T0, T1, T2, T3> extends AbstractTupleMapper<T, Quad<T0, T1, T2, T3>> {

  /**
   * Constructs a new QuadMapperImpl with the specified mapping functions.
   *
   * @param m0 the function to map to the first element
   * @param m1 the function to map to the second element
   * @param m2 the function to map to the third element
   * @param m3 the function to map to the fourth element
   */
  public QuadMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3) {
    super(4);
    set(0, m0);
    set(1, m1);
    set(2, m2);
    set(3, m3);
  }

  @Override
  public Quad<T0, T1, T2, T3> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t), getFourth().apply(t));
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
}