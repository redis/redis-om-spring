package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Single;

/**
 * Implementation of a 1-element tuple (Single).
 * <p>
 * This class provides a concrete implementation for holding exactly one element
 * in a type-safe manner with labeled access. While a single-element tuple may
 * seem redundant, it provides consistency in tuple-based APIs and can be useful
 * for generic programming scenarios where the number of elements may vary.
 * </p>
 *
 * @param <T0> the type of the first (and only) element
 */
public final class SingleImpl<T0> extends AbstractTuple implements Single<T0> {

  /**
   * Constructs a new SingleImpl with the specified labels and element.
   *
   * @param labels the labels for the tuple element (should contain exactly one label)
   * @param e0     the first (and only) element of the single
   */
  public SingleImpl(String[] labels, T0 e0) {
    super(SingleImpl.class, labels, e0);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T0 getFirst() {
    return ((T0) values[0]);
  }
}