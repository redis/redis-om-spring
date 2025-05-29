package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Vigintuple;

/**
 * Implementation of a 20-element tuple (Vigintuple).
 * This class provides a concrete implementation for holding exactly twenty elements
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
 * @param <T11> the type of the twelfth element
 * @param <T12> the type of the thirteenth element
 * @param <T13> the type of the fourteenth element
 * @param <T14> the type of the fifteenth element
 * @param <T15> the type of the sixteenth element
 * @param <T16> the type of the seventeenth element
 * @param <T17> the type of the eighteenth element
 * @param <T18> the type of the nineteenth element
 * @param <T19> the type of the twentieth element
 */
public final class VigintupleImpl<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>
    extends AbstractTuple implements
    Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> {

  /**
   * Constructs a new VigintupleImpl with the specified labels and elements.
   *
   * @param labels the labels for the tuple elements
   * @param e0     the first element
   * @param e1     the second element
   * @param e2     the third element
   * @param e3     the fourth element
   * @param e4     the fifth element
   * @param e5     the sixth element
   * @param e6     the seventh element
   * @param e7     the eighth element
   * @param e8     the ninth element
   * @param e9     the tenth element
   * @param e10    the eleventh element
   * @param e11    the twelfth element
   * @param e12    the thirteenth element
   * @param e13    the fourteenth element
   * @param e14    the fifteenth element
   * @param e15    the sixteenth element
   * @param e16    the seventeenth element
   * @param e17    the eighteenth element
   * @param e18    the nineteenth element
   * @param e19    the twentieth element
   */
  public VigintupleImpl(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10,
      T11 e11, T12 e12, T13 e13, T14 e14, T15 e15, T16 e16, T17 e17, T18 e18, T19 e19) {
    super(VigintupleImpl.class, labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17,
        e18, e19);
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

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T11 getTwelfth() {
    return ((T11) values[11]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T12 getThirteenth() {
    return ((T12) values[12]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T13 getFourteenth() {
    return ((T13) values[13]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T14 getFifteenth() {
    return ((T14) values[14]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T15 getSixteenth() {
    return ((T15) values[15]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T16 getSeventeenth() {
    return ((T16) values[16]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T17 getEighteenth() {
    return ((T17) values[17]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T18 getNineteenth() {
    return ((T18) values[18]);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public T19 getTwentieth() {
    return ((T19) values[19]);
  }
}