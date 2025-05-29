package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Decuple;

/**
 * Implementation of Decuple (10-element tuple).
 * 
 * @param <T0> type of first element
 * @param <T1> type of second element
 * @param <T2> type of third element
 * @param <T3> type of fourth element
 * @param <T4> type of fifth element
 * @param <T5> type of sixth element
 * @param <T6> type of seventh element
 * @param <T7> type of eighth element
 * @param <T8> type of ninth element
 * @param <T9> type of tenth element
 */
public final class DecupleImpl<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> extends AbstractTuple implements
    Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> {

  /**
   * Creates a new DecupleImpl with the given values.
   * 
   * @param labels the field labels
   * @param e0     first element
   * @param e1     second element
   * @param e2     third element
   * @param e3     fourth element
   * @param e4     fifth element
   * @param e5     sixth element
   * @param e6     seventh element
   * @param e7     eighth element
   * @param e8     ninth element
   * @param e9     tenth element
   */
  public DecupleImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9) {
    super(DecupleImpl.class, labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9);
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

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T8 getNinth() {
    return ((T8) values[8]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T9 getTenth() {
    return ((T9) values[9]);
  }
}