package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Pair;

/**
 * Implementation of a 2-element tuple (Pair).
 * This class provides a concrete implementation for holding exactly two elements
 * in a type-safe manner with labeled access.
 *
 * @param <T0> the type of the first element
 * @param <T1> the type of the second element
 */
public final class PairImpl<T0, T1> extends AbstractTuple implements Pair<T0, T1> {

  /**
   * Constructs a new PairImpl with the specified labels and elements.
   *
   * @param labels the labels for the tuple elements
   * @param e0     the first element of the pair
   * @param e1     the second element of the pair
   */
  public PairImpl(String[] labels, T0 e0, T1 e1) {
    super(PairImpl.class, labels, e0, e1);
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
}