package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Triple;

public final class TripleImpl<T0, T1, T2> extends AbstractTuple implements Triple<T0, T1, T2> {

  public TripleImpl(String[] labels, T0 e0, T1 e1, T2 e2) {
    super(TripleImpl.class, labels, e0, e1, e2);
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
}