package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.impl.EmptyTupleImpl;
import com.redis.om.spring.tuple.impl.DecupleImpl;
import com.redis.om.spring.tuple.impl.UndecupleImpl;
import com.redis.om.spring.tuple.impl.DuodecupleImpl;
import com.redis.om.spring.tuple.impl.TredecupleImpl;
import com.redis.om.spring.tuple.impl.QuattuordecupleImpl;
import com.redis.om.spring.tuple.impl.QuindecupleImpl;
import com.redis.om.spring.tuple.impl.SexdecupleImpl;
import com.redis.om.spring.tuple.impl.SeptendecupleImpl;
import com.redis.om.spring.tuple.impl.OctodecupleImpl;
import com.redis.om.spring.tuple.impl.NovemdecupleImpl;
import com.redis.om.spring.tuple.impl.SingleImpl;
import com.redis.om.spring.tuple.impl.VigintupleImpl;
import com.redis.om.spring.tuple.impl.PairImpl;
import com.redis.om.spring.tuple.impl.TripleImpl;
import com.redis.om.spring.tuple.impl.QuadImpl;
import com.redis.om.spring.tuple.impl.QuintupleImpl;
import com.redis.om.spring.tuple.impl.HextupleImpl;
import com.redis.om.spring.tuple.impl.SeptupleImpl;
import com.redis.om.spring.tuple.impl.OctupleImpl;
import com.redis.om.spring.tuple.impl.NonupleImpl;

public final class Tuples {

  private Tuples() {}

  public static EmptyTuple of() {
    return EmptyTupleImpl.EMPTY_TUPLE;
  }

  public static <T0> Single<T0> of(T0 e0) {
    return new SingleImpl<>(e0);
  }

  public static <T0, T1> Pair<T0, T1> of(T0 e0, T1 e1) {
    return new PairImpl<>(e0, e1);
  }

  public static <T0, T1, T2> Triple<T0, T1, T2> of(T0 e0, T1 e1, T2 e2) {
    return new TripleImpl<>(e0, e1, e2);
  }

  public static <T0, T1, T2, T3> Quad<T0, T1, T2, T3> of(T0 e0, T1 e1, T2 e2, T3 e3) {
    return new QuadImpl<>(e0, e1, e2, e3);
  }

  public static <T0, T1, T2, T3, T4> Quintuple<T0, T1, T2, T3, T4> of(T0 e0, T1 e1, T2 e2, T3 e3, T4 e4) {
    return new QuintupleImpl<>(e0, e1, e2, e3, e4);
  }

  public static <T0, T1, T2, T3, T4, T5> Hextuple<T0, T1, T2, T3, T4, T5> of(T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5) {
    return new HextupleImpl<>(e0, e1, e2, e3, e4, e5);
  }

  public static <T0, T1, T2, T3, T4, T5, T6> Septuple<T0, T1, T2, T3, T4, T5, T6> of(T0 e0, T1 e1, T2 e2, T3 e3, T4 e4,
      T5 e5, T6 e6) {
    return new SeptupleImpl<>(e0, e1, e2, e3, e4, e5, e6);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7> Octuple<T0, T1, T2, T3, T4, T5, T6, T7> of(T0 e0, T1 e1, T2 e2, T3 e3,
      T4 e4, T5 e5, T6 e6, T7 e7) {
    return new OctupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8> Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8> of(T0 e0, T1 e1, T2 e2,
      T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8) {
    return new NonupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T0 e0,
      T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9) {
    return new DecupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Undecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10) {
    return new UndecupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Duodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11) {
    return new DuodecupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12) {
    return new TredecupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Quattuordecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13) {
    return new QuattuordecupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13,
      T14 e14) {
    return new QuindecupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Sexdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15) {
    return new SexdecupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Septendecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16) {
    return new SeptendecupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> Octodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17) {
    return new OctodecupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17, T18 e18) {
    return new NovemdecupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17, T18 e18, T19 e19) {
    return new VigintupleImpl<>(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18,
        e19);
  }

  @SafeVarargs
  public static Tuple ofArray(Object... el) {
    switch (el.length) {
      case 0:
        return of();
      case 1:
        return of(el[0]);
      case 2:
        return of(el[0], el[1]);
      case 3:
        return of(el[0], el[1], el[2]);
      case 4:
        return of(el[0], el[1], el[2], el[3]);
      case 5:
        return of(el[0], el[1], el[2], el[3], el[4]);
      case 6:
        return of(el[0], el[1], el[2], el[3], el[4], el[5]);
      case 7:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6]);
      case 8:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7]);
      case 9:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8]);
      case 10:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9]);
      case 11:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10]);
      case 12:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11]);
      case 13:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12]);
      case 14:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13]);
      case 15:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14]);
      case 16:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14], el[15]);
      case 17:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14], el[15], el[16]);
      case 18:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14], el[15], el[16], el[17]);
      case 19:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14], el[15], el[16], el[17], el[18]);
      case 20:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14], el[15], el[16], el[17], el[18], el[19]);
      default:
        return new TupleInfiniteDegreeImpl(el);
    }
  }
}
