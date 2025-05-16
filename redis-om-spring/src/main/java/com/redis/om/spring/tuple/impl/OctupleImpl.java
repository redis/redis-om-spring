package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Octuple;

public final class OctupleImpl<T0, T1, T2, T3, T4, T5, T6, T7> extends AbstractTuple implements
    Octuple<T0, T1, T2, T3, T4, T5, T6, T7> {

  public OctupleImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7) {
    super(OctupleImpl.class, labels, e0, e1, e2, e3, e4, e5, e6, e7);
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

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T4 getFifth() {
    return ((T4) values[4]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T5 getSixth() {
    return ((T5) values[5]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T6 getSeventh() {
    return ((T6) values[6]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T7 getEighth() {
    return ((T7) values[7]);
  }
}