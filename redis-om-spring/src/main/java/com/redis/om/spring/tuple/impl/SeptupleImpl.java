package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Septuple;

/**
 * Implementation of a 7-element tuple (septuple) for the Redis OM Spring framework.
 * <p>
 * This class provides a concrete implementation of the Septuple interface, allowing
 * storage and retrieval of seven strongly-typed elements. It is part of Redis OM Spring's
 * tuple system that enables structured data handling in search results, aggregations,
 * and data projections.
 * <p>
 * The septuple maintains type safety for all seven elements while providing indexed
 * access through specific getter methods. This implementation is immutable once created
 * and extends AbstractTuple for common tuple functionality.
 * <p>
 * Typical usage in Redis OM Spring:
 * <pre>
 * {@code
 * // Creating a septuple for search projections
 * Septuple<String, Integer, Double, Boolean, Date, Long, Float> result = 
 *     Tuples.of("name", 25, 3.14, true, new Date(), 1000L, 2.5f);
 * 
 * // Accessing elements
 * String first = result.getFirst();
 * Integer second = result.getSecond();
 * }
 * </pre>
 *
 * @param <T0> the type of the first element
 * @param <T1> the type of the second element
 * @param <T2> the type of the third element
 * @param <T3> the type of the fourth element
 * @param <T4> the type of the fifth element
 * @param <T5> the type of the sixth element
 * @param <T6> the type of the seventh element
 * 
 * @see Septuple
 * @see AbstractTuple
 * @see com.redis.om.spring.tuple.Tuples
 * 
 * @author Redis OM Spring Team
 * @since 1.0.0
 */
public final class SeptupleImpl<T0, T1, T2, T3, T4, T5, T6> extends AbstractTuple implements
    Septuple<T0, T1, T2, T3, T4, T5, T6> {

  /**
   * Constructs a new SeptupleImpl with the specified labels and values.
   * <p>
   * Creates an immutable septuple containing seven elements with optional field labels.
   * The labels array should contain seven elements corresponding to each tuple position,
   * or can be null if labels are not needed.
   * <p>
   * This constructor is typically called by the Tuples factory class or during
   * search result mapping operations in Redis OM Spring.
   *
   * @param labels an array of field labels for the tuple elements, must be null or contain exactly 7 elements
   * @param e0     the first element of the tuple
   * @param e1     the second element of the tuple
   * @param e2     the third element of the tuple
   * @param e3     the fourth element of the tuple
   * @param e4     the fifth element of the tuple
   * @param e5     the sixth element of the tuple
   * @param e6     the seventh element of the tuple
   * 
   * @throws IllegalArgumentException if labels array is provided but doesn't contain exactly 7 elements
   */
  public SeptupleImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6) {
    super(SeptupleImpl.class, labels, e0, e1, e2, e3, e4, e5, e6);
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
}