package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Single;

public final class SingleImpl<T0> extends AbstractTuple implements Single<T0> {

  public SingleImpl(String[] labels, T0 e0) {
    super(SingleImpl.class, labels, e0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T0 getFirst() {
    return ((T0) values[0]);
  }
}