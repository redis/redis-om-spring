package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Hextuple;

/**
 * Implementation of a tuple of degree 6 holding six elements.
 *
 * @param <T0> the type of the first element
 * @param <T1> the type of the second element
 * @param <T2> the type of the third element
 * @param <T3> the type of the fourth element
 * @param <T4> the type of the fifth element
 * @param <T5> the type of the sixth element
 */
public final class HextupleImpl<T0, T1, T2, T3, T4, T5> extends AbstractTuple implements
    Hextuple<T0, T1, T2, T3, T4, T5> {

  /**
   * Constructs a new HextupleImpl with the specified labels and elements.
   *
   * @param labels the labels for the tuple elements
   * @param e0     the first element
   * @param e1     the second element
   * @param e2     the third element
   * @param e3     the fourth element
   * @param e4     the fifth element
   * @param e5     the sixth element
   */
  public HextupleImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5) {
    super(HextupleImpl.class, labels, e0, e1, e2, e3, e4, e5);
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
}