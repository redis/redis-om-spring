package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Pair;

public final class PairImpl<T0, T1> extends AbstractTuple implements Pair<T0, T1> {

  public PairImpl(String[] labels, T0 e0, T1 e1) {
    super(PairImpl.class, labels, e0, e1);
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
}