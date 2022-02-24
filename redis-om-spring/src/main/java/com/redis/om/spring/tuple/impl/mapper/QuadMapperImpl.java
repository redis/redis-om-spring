package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Quad;
import com.redis.om.spring.tuple.TupleMapper;
import com.redis.om.spring.tuple.Tuples;

public final class QuadMapperImpl<T, T0, T1, T2, T3> extends AbstractTupleMapper<T, Quad<T0, T1, T2, T3>>
    implements TupleMapper<T, Quad<T0, T1, T2, T3>> {

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

  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }

  public Function<T, T2> getThird() {
    return getAndCast(2);
  }

  public Function<T, T3> getFourth() {
    return getAndCast(3);
  }
}