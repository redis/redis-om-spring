package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Quintuple;

public final class QuintupleImpl<T0, T1, T2, T3, T4> extends AbstractTuple implements Quintuple<T0, T1, T2, T3, T4> {

  public QuintupleImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4) {
    super(QuintupleImpl.class, labels, e0, e1, e2, e3, e4);
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
}