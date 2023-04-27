package com.redis.om.spring.tuple.impl.mapper;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Triple;
import com.redis.om.spring.tuple.Tuples;

import java.util.function.Function;

public final class TripleMapperImpl<T, T0, T1, T2> extends AbstractTupleMapper<T, Triple<T0, T1, T2>> {

  public TripleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2) {
    super(3);
    set(0, m0);
    set(1, m1);
    set(2, m2);
  }

  @Override
  public Triple<T0, T1, T2> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t));
  }

  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }

  public Function<T, T2> getThird() {
    return getAndCast(2);
  }
}