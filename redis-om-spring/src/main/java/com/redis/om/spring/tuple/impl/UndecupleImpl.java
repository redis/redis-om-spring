package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Undecuple;

/**
 * Implementation of an 11-element tuple (Undecuple).
 * This class provides a concrete implementation for holding exactly eleven elements
 * in a type-safe manner with labeled access.
 *
 * @param <T0>  the type of the first element
 * @param <T1>  the type of the second element
 * @param <T2>  the type of the third element
 * @param <T3>  the type of the fourth element
 * @param <T4>  the type of the fifth element
 * @param <T5>  the type of the sixth element
 * @param <T6>  the type of the seventh element
 * @param <T7>  the type of the eighth element
 * @param <T8>  the type of the ninth element
 * @param <T9>  the type of the tenth element
 * @param <T10> the type of the eleventh element
 */
public final class UndecupleImpl<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> extends AbstractTuple implements
    Undecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> {

  /**
   * Constructs a new UndecupleImpl with the specified labels and elements.
   *
   * @param labels the labels for the tuple elements
   * @param e0     the first element of the undecuple
   * @param e1     the second element of the undecuple
   * @param e2     the third element of the undecuple
   * @param e3     the fourth element of the undecuple
   * @param e4     the fifth element of the undecuple
   * @param e5     the sixth element of the undecuple
   * @param e6     the seventh element of the undecuple
   * @param e7     the eighth element of the undecuple
   * @param e8     the ninth element of the undecuple
   * @param e9     the tenth element of the undecuple
   * @param e10    the eleventh element of the undecuple
   */
  public UndecupleImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10) {
    super(UndecupleImpl.class, labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
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

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T10 getEleventh() {
    return ((T10) values[10]);
  }
}