package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Single;
import com.redis.om.spring.tuple.Tuples;

/**
 * Implementation of a tuple mapper for 1-element tuples (Single).
 * This class provides functionality to map from a source type T to a Single containing one mapped value.
 *
 * @param <T>  the input type to map from
 * @param <T0> the type of the element in the resulting single
 */
public final class SingleMapperImpl<T, T0> extends AbstractTupleMapper<T, Single<T0>> {

  /**
   * Constructs a new SingleMapperImpl with the specified mapping function.
   *
   * @param m0 the function to map to the single element
   */
  public SingleMapperImpl(Function<T, T0> m0) {
    super(1);
    set(0, m0);
  }

  @Override
  public Single<T0> apply(T t) {
    return Tuples.of(getFirst().apply(t));
  }

  /**
   * Gets the mapping function for the first (and only) element.
   *
   * @return the function that maps to the first element
   */
  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }
}