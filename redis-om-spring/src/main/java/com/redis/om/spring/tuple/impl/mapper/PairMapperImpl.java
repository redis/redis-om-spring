package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Tuples;

public final class PairMapperImpl<T, T0, T1> extends AbstractTupleMapper<T, Pair<T0, T1>> {

  public PairMapperImpl(Function<T, T0> m0, Function<T, T1> m1) {
    super(2);
    set(0, m0);
    set(1, m1);
  }

  @Override
  public Pair<T0, T1> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t));
  }

  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }
}