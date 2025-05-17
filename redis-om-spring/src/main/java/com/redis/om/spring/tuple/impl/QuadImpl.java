package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Quad;

public final class QuadImpl<T0, T1, T2, T3> extends AbstractTuple implements Quad<T0, T1, T2, T3> {

  public QuadImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3) {
    super(QuadImpl.class, labels, e0, e1, e2, e3);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T0 getFirst() {
    return ((T0) values[0]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T1 getSecond() {
    return ((T1) values[1]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T2 getThird() {
    return ((T2) values[2]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T3 getFourth() {
    return ((T3) values[3]);
  }
}