package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Septendecuple;

/**
 * Implementation of a 17-element tuple (Septendecuple).
 * This class provides a concrete implementation for holding exactly seventeen elements
 * in a type-safe manner with labeled access. Septendecuples are useful in Redis OM Spring
 * for complex query results, aggregation operations, and data projection scenarios where
 * seventeen related values need to be grouped together while maintaining type safety.
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
 * @param <T11> the type of the twelfth element
 * @param <T12> the type of the thirteenth element
 * @param <T13> the type of the fourteenth element
 * @param <T14> the type of the fifteenth element
 * @param <T15> the type of the sixteenth element
 * @param <T16> the type of the seventeenth element
 */
public final class SeptendecupleImpl<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> extends
    AbstractTuple implements Septendecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> {

  /**
   * Constructs a new SeptendecupleImpl with the specified labels and elements.
   *
   * @param labels the labels for the tuple elements
   * @param e0     the first element of the septendecuple
   * @param e1     the second element of the septendecuple
   * @param e2     the third element of the septendecuple
   * @param e3     the fourth element of the septendecuple
   * @param e4     the fifth element of the septendecuple
   * @param e5     the sixth element of the septendecuple
   * @param e6     the seventh element of the septendecuple
   * @param e7     the eighth element of the septendecuple
   * @param e8     the ninth element of the septendecuple
   * @param e9     the tenth element of the septendecuple
   * @param e10    the eleventh element of the septendecuple
   * @param e11    the twelfth element of the septendecuple
   * @param e12    the thirteenth element of the septendecuple
   * @param e13    the fourteenth element of the septendecuple
   * @param e14    the fifteenth element of the septendecuple
   * @param e15    the sixteenth element of the septendecuple
   * @param e16    the seventeenth element of the septendecuple
   */
  public SeptendecupleImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9,
      T10 e10, T11 e11, T12 e12, T13 e13, T14 e14, T15 e15, T16 e16) {
    super(SeptendecupleImpl.class, labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16);
  }

  /**
   * Gets the first element of the septendecuple.
   *
   * @return the first element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T0 getFirst() {
    return ((T0) values[0]);
  }

  /**
   * Gets the second element of the septendecuple.
   *
   * @return the second element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T1 getSecond() {
    return ((T1) values[1]);
  }

  /**
   * Gets the third element of the septendecuple.
   *
   * @return the third element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T2 getThird() {
    return ((T2) values[2]);
  }

  /**
   * Gets the fourth element of the septendecuple.
   *
   * @return the fourth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T3 getFourth() {
    return ((T3) values[3]);
  }

  /**
   * Gets the fifth element of the septendecuple.
   *
   * @return the fifth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T4 getFifth() {
    return ((T4) values[4]);
  }

  /**
   * Gets the sixth element of the septendecuple.
   *
   * @return the sixth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T5 getSixth() {
    return ((T5) values[5]);
  }

  /**
   * Gets the seventh element of the septendecuple.
   *
   * @return the seventh element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T6 getSeventh() {
    return ((T6) values[6]);
  }

  /**
   * Gets the eighth element of the septendecuple.
   *
   * @return the eighth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T7 getEighth() {
    return ((T7) values[7]);
  }

  /**
   * Gets the ninth element of the septendecuple.
   *
   * @return the ninth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T8 getNinth() {
    return ((T8) values[8]);
  }

  /**
   * Gets the tenth element of the septendecuple.
   *
   * @return the tenth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T9 getTenth() {
    return ((T9) values[9]);
  }

  /**
   * Gets the eleventh element of the septendecuple.
   *
   * @return the eleventh element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T10 getEleventh() {
    return ((T10) values[10]);
  }

  /**
   * Gets the twelfth element of the septendecuple.
   *
   * @return the twelfth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T11 getTwelfth() {
    return ((T11) values[11]);
  }

  /**
   * Gets the thirteenth element of the septendecuple.
   *
   * @return the thirteenth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T12 getThirteenth() {
    return ((T12) values[12]);
  }

  /**
   * Gets the fourteenth element of the septendecuple.
   *
   * @return the fourteenth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T13 getFourteenth() {
    return ((T13) values[13]);
  }

  /**
   * Gets the fifteenth element of the septendecuple.
   *
   * @return the fifteenth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T14 getFifteenth() {
    return ((T14) values[14]);
  }

  /**
   * Gets the sixteenth element of the septendecuple.
   *
   * @return the sixteenth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T15 getSixteenth() {
    return ((T15) values[15]);
  }

  /**
   * Gets the seventeenth element of the septendecuple.
   *
   * @return the seventeenth element
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T16 getSeventeenth() {
    return ((T16) values[16]);
  }
}