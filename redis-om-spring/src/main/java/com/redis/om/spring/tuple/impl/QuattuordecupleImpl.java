package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Quattuordecuple;

public final class QuattuordecupleImpl<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> extends AbstractTuple
    implements Quattuordecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> {

  public QuattuordecupleImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11,
      T12 e12, T13 e13) {
    super(QuattuordecupleImpl.class, labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13);
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

  @SuppressWarnings("unchecked")
  @Override
  public T7 getEighth() {
    return ((T7) values[7]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T8 getNinth() {
    return ((T8) values[8]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T9 getTenth() {
    return ((T9) values[9]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T10 getEleventh() {
    return ((T10) values[10]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T11 getTwelfth() {
    return ((T11) values[11]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T12 getThirteenth() {
    return ((T12) values[12]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T13 getFourteenth() {
    return ((T13) values[13]);
  }
}