package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Tuples;

/**
 * Implementation of a tuple mapper for 2-element tuples (Pair).
 * This class provides functionality to map from a source type T to a Pair containing two mapped values.
 *
 * @param <T>  the input type to map from
 * @param <T0> the type of the first element in the resulting pair
 * @param <T1> the type of the second element in the resulting pair
 */
public final class PairMapperImpl<T, T0, T1> extends AbstractTupleMapper<T, Pair<T0, T1>> {

  /**
   * Constructs a new PairMapperImpl with the specified mapping functions.
   *
   * @param m0 the function to map to the first element
   * @param m1 the function to map to the second element
   */
  public PairMapperImpl(Function<T, T0> m0, Function<T, T1> m1) {
    super(2);
    set(0, m0);
    set(1, m1);
  }

  @Override
  public Pair<T0, T1> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t));
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
}