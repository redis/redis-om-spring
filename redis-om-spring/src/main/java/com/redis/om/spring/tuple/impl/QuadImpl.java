package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Quad;

/**
 * Implementation of a 4-element tuple (Quad).
 * This class provides a concrete implementation for holding exactly four elements
 * in a type-safe manner with labeled access.
 *
 * @param <T0> the type of the first element
 * @param <T1> the type of the second element
 * @param <T2> the type of the third element
 * @param <T3> the type of the fourth element
 */
public final class QuadImpl<T0, T1, T2, T3> extends AbstractTuple implements Quad<T0, T1, T2, T3> {

  /**
   * Constructs a new QuadImpl with the specified labels and elements.
   *
   * @param labels the labels for the tuple elements
   * @param e0     the first element of the quad
   * @param e1     the second element of the quad
   * @param e2     the third element of the quad
   * @param e3     the fourth element of the quad
   */
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