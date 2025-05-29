package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Quintuple;

/**
 * Implementation of a 5-element tuple (Quintuple).
 * This class provides a concrete implementation for holding exactly five elements
 * in a type-safe manner with labeled access.
 *
 * @param <T0> the type of the first element
 * @param <T1> the type of the second element
 * @param <T2> the type of the third element
 * @param <T3> the type of the fourth element
 * @param <T4> the type of the fifth element
 */
public final class QuintupleImpl<T0, T1, T2, T3, T4> extends AbstractTuple implements Quintuple<T0, T1, T2, T3, T4> {

  /**
   * Constructs a new QuintupleImpl with the specified labels and elements.
   *
   * @param labels the labels for the tuple elements
   * @param e0     the first element of the quintuple
   * @param e1     the second element of the quintuple
   * @param e2     the third element of the quintuple
   * @param e3     the fourth element of the quintuple
   * @param e4     the fifth element of the quintuple
   */
  public QuintupleImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4) {
    super(QuintupleImpl.class, labels, e0, e1, e2, e3, e4);
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
}