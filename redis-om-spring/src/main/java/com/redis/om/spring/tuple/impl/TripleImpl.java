package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Triple;

/**
 * Implementation class for 3-element tuples.
 * <p>
 * This class provides a concrete implementation of the {@link Triple} interface,
 * representing an immutable tuple containing exactly three elements. It extends
 * {@link AbstractTuple} to inherit common tuple functionality while implementing
 * the specific methods required for accessing the three elements.
 * </p>
 * <p>
 * The implementation stores the three elements internally and provides type-safe
 * accessors through the {@link #getFirst()}, {@link #getSecond()}, and
 * {@link #getThird()} methods. The elements can be of different types as specified
 * by the generic type parameters.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * Triple<String, Integer, Boolean> triple = new TripleImpl<>(
 * new String[]{"name", "age", "active"},
 * "John", 30, true
 * );
 * }</pre>
 *
 * @param <T0> the type of the first element
 * @param <T1> the type of the second element
 * @param <T2> the type of the third element
 * @see Triple
 * @see AbstractTuple
 * @since 0.1.0
 */
public final class TripleImpl<T0, T1, T2> extends AbstractTuple implements Triple<T0, T1, T2> {

  /**
   * Constructs a new TripleImpl with the specified labels and elements.
   *
   * @param labels an array of labels for the tuple elements, should contain exactly 3 labels
   * @param e0     the first element of the tuple
   * @param e1     the second element of the tuple
   * @param e2     the third element of the tuple
   */
  public TripleImpl(String[] labels, T0 e0, T1 e1, T2 e2) {
    super(TripleImpl.class, labels, e0, e1, e2);
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
}