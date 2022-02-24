package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.AbstractTuple;
import com.redis.om.spring.tuple.Single;

public final class SingleImpl<T0> extends AbstractTuple implements Single<T0> {

  public SingleImpl(T0 e0) {
    super(SingleImpl.class, e0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T0 getFirst() {
    return ((T0) values[0]);
  }
}