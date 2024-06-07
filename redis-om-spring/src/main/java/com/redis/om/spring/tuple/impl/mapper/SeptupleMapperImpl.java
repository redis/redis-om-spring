package com.redis.om.spring.tuple.impl.mapper;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Septuple;
import com.redis.om.spring.tuple.Tuples;

import java.util.function.Function;

public final class SeptupleMapperImpl<T, T0, T1, T2, T3, T4, T5, T6>
    extends AbstractTupleMapper<T, Septuple<T0, T1, T2, T3, T4, T5, T6>> {

  public SeptupleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3,
      Function<T, T4> m4, Function<T, T5> m5, Function<T, T6> m6) {
    super(7);
    set(0, m0);
    set(1, m1);
    set(2, m2);
    set(3, m3);
    set(4, m4);
    set(5, m5);
    set(6, m6);
  }

  @Override
  public Septuple<T0, T1, T2, T3, T4, T5, T6> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t), getFourth().apply(t),
        getFifth().apply(t), getSixth().apply(t), getSeventh().apply(t));
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

  public Function<T, T4> getFifth() {
    return getAndCast(4);
  }

  public Function<T, T5> getSixth() {
    return getAndCast(5);
  }

  public Function<T, T6> getSeventh() {
    return getAndCast(6);
  }
}