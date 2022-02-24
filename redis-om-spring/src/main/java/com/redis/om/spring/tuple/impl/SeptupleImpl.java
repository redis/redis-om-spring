package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.AbstractTuple;
import com.redis.om.spring.tuple.Septuple;

public final class SeptupleImpl<T0, T1, T2, T3, T4, T5, T6> extends AbstractTuple
    implements Septuple<T0, T1, T2, T3, T4, T5, T6> {

  public SeptupleImpl(T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6) {
    super(SeptupleImpl.class, e0, e1, e2, e3, e4, e5, e6);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T0 getFirst() {
    return ((T0) values[0]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T1 getSecond() {
    return ((T1) values[1]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T2 getThird() {
    return ((T2) values[2]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T3 getFourth() {
    return ((T3) values[3]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T4 getFifth() {
    return ((T4) values[4]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T5 getSixth() {
    return ((T5) values[5]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T6 getSeventh() {
    return ((T6) values[6]);
  }
}