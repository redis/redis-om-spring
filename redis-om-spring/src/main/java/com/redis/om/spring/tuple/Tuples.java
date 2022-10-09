package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.impl.EmptyTupleImpl;

import java.util.function.Function;

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
import com.redis.om.spring.tuple.impl.mapper.DecupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.DuodecupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.EmptyTupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.HextupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.NonupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.NovemdecupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.OctodecupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.OctupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.PairMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.QuadMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.QuattuordecupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.QuindecupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.QuintupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.SeptendecupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.SeptupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.SexdecupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.SingleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.TredecupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.TripleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.UndecupleMapperImpl;
import com.redis.om.spring.tuple.impl.mapper.VigintupleMapperImpl;
import com.redis.om.spring.tuple.impl.PairImpl;
import com.redis.om.spring.tuple.impl.TripleImpl;
import com.redis.om.spring.tuple.impl.TupleInfiniteDegreeImpl;
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
    return new SingleImpl<>(new String[] {}, e0);
  }
  
  public static <T0> Single<T0> of(String[] labels, T0 e0) {
    return new SingleImpl<>(labels, e0);
  }

  public static <T0, T1> Pair<T0, T1> of(T0 e0, T1 e1) {
    return new PairImpl<>(new String[] {}, e0, e1);
  }
  
  public static <T0, T1> Pair<T0, T1> of(String[] labels, T0 e0, T1 e1) {
    return new PairImpl<>(labels, e0, e1);
  }

  public static <T0, T1, T2> Triple<T0, T1, T2> of(T0 e0, T1 e1, T2 e2) {
    return new TripleImpl<>(new String[] {}, e0, e1, e2);
  }
  
  public static <T0, T1, T2> Triple<T0, T1, T2> of(String[] labels, T0 e0, T1 e1, T2 e2) {
    return new TripleImpl<>(labels, e0, e1, e2);
  }

  public static <T0, T1, T2, T3> Quad<T0, T1, T2, T3> of(T0 e0, T1 e1, T2 e2, T3 e3) {
    return new QuadImpl<>(new String[] {}, e0, e1, e2, e3);
  }
  
  public static <T0, T1, T2, T3> Quad<T0, T1, T2, T3> of(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3) {
    return new QuadImpl<>(labels, e0, e1, e2, e3);
  }

  public static <T0, T1, T2, T3, T4> Quintuple<T0, T1, T2, T3, T4> of(T0 e0, T1 e1, T2 e2, T3 e3, T4 e4) {
    return new QuintupleImpl<>(new String[] {}, e0, e1, e2, e3, e4);
  }
  
  public static <T0, T1, T2, T3, T4> Quintuple<T0, T1, T2, T3, T4> of(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4) {
    return new QuintupleImpl<>(labels, e0, e1, e2, e3, e4);
  }

  public static <T0, T1, T2, T3, T4, T5> Hextuple<T0, T1, T2, T3, T4, T5> of(T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5) {
    return new HextupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5);
  }
  
  public static <T0, T1, T2, T3, T4, T5> Hextuple<T0, T1, T2, T3, T4, T5> of(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5) {
    return new HextupleImpl<>(labels, e0, e1, e2, e3, e4, e5);
  }

  public static <T0, T1, T2, T3, T4, T5, T6> Septuple<T0, T1, T2, T3, T4, T5, T6> of(T0 e0, T1 e1, T2 e2, T3 e3, T4 e4,
      T5 e5, T6 e6) {
    return new SeptupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6> Septuple<T0, T1, T2, T3, T4, T5, T6> of(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4,
      T5 e5, T6 e6) {
    return new SeptupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7> Octuple<T0, T1, T2, T3, T4, T5, T6, T7> of(T0 e0, T1 e1, T2 e2, T3 e3,
      T4 e4, T5 e5, T6 e6, T7 e7) {
    return new OctupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7> Octuple<T0, T1, T2, T3, T4, T5, T6, T7> of(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3,
      T4 e4, T5 e5, T6 e6, T7 e7) {
    return new OctupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8> Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8> of(T0 e0, T1 e1, T2 e2,
      T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8) {
    return new NonupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8> Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8> of(String[] labels, T0 e0, T1 e1, T2 e2,
      T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8) {
    return new NonupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T0 e0,
      T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9) {
    return new DecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> of(String[] labels, T0 e0,
      T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9) {
    return new DecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Undecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10) {
    return new UndecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Undecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(String[] labels, 
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10) {
    return new UndecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Duodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11) {
    return new DuodecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Duodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> of(String[] labels,
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11) {
    return new DuodecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12) {
    return new TredecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> of(String[] labels,
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12) {
    return new TredecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Quattuordecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13) {
    return new QuattuordecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13);
  }


  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Quattuordecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> of(String[] labels,
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13) {
    return new QuattuordecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13,
      T14 e14) {
    return new QuindecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> of(String[] labels,
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13,
      T14 e14) {
    return new QuindecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Sexdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15) {
    return new SexdecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Sexdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> of(String[] labels,
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15) {
    return new SexdecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Septendecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16) {
    return new SeptendecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Septendecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> of(String[] labels,
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16) {
    return new SeptendecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> Octodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17) {
    return new OctodecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> Octodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> of(String[] labels,
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17) {
    return new OctodecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17, T18 e18) {
    return new NovemdecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> of(String[] labels,
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17, T18 e18) {
    return new NovemdecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18);
  }

  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17, T18 e18, T19 e19) {
    return new VigintupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18,
        e19);
  }
  
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> of(String[] labels,
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17, T18 e18, T19 e19) {
    return new VigintupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18,
        e19);
  }

  @SafeVarargs
  public static Tuple ofArray(String[] returnFields, Object... el) {
    switch (el.length) {
      case 0:
        return of();
      case 1:
        return of(returnFields, el[0]);
      case 2:
        return of(returnFields, el[0], el[1]);
      case 3:
        return of(returnFields, el[0], el[1], el[2]);
      case 4:
        return of(returnFields, el[0], el[1], el[2], el[3]);
      case 5:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4]);
      case 6:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5]);
      case 7:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6]);
      case 8:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7]);
      case 9:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8]);
      case 10:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9]);
      case 11:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10]);
      case 12:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11]);
      case 13:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12]);
      case 14:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13]);
      case 15:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14]);
      case 16:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14], el[15]);
      case 17:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14], el[15], el[16]);
      case 18:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14], el[15], el[16], el[17]);
      case 19:
        return of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14], el[15], el[16], el[17], el[18]);
      case 20:
        return of(el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11], el[12], el[13],
            el[14], el[15], el[16], el[17], el[18], el[19]);
      default:
        return new TupleInfiniteDegreeImpl(el);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> Function<T, EmptyTuple> toTuple() {
    return (Function<T, EmptyTuple>) EmptyTupleMapperImpl.EMPTY_MAPPER;
  }

  public static <T, T0> Function<T, Single<T0>> toTuple(Function<T, T0> m0) {
    return new SingleMapperImpl<>(m0);
  }

  public static <T, T0, T1> Function<T, Pair<T0, T1>> toTuple(Function<T, T0> m0, Function<T, T1> m1) {
    return new PairMapperImpl<>(m0, m1);
  }

  public static <T, T0, T1, T2> Function<T, Triple<T0, T1, T2>> toTuple(Function<T, T0> m0, Function<T, T1> m1,
      Function<T, T2> m2) {
    return new TripleMapperImpl<>(m0, m1, m2);
  }

  public static <T, T0, T1, T2, T3> Function<T, Quad<T0, T1, T2, T3>> toTuple(Function<T, T0> m0, Function<T, T1> m1,
      Function<T, T2> m2, Function<T, T3> m3) {
    return new QuadMapperImpl<>(m0, m1, m2, m3);
  }

  public static <T, T0, T1, T2, T3, T4> Function<T, Quintuple<T0, T1, T2, T3, T4>> toTuple(Function<T, T0> m0,
      Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4) {
    return new QuintupleMapperImpl<>(m0, m1, m2, m3, m4);
  }

  public static <T, T0, T1, T2, T3, T4, T5> Function<T, Hextuple<T0, T1, T2, T3, T4, T5>> toTuple(Function<T, T0> m0,
      Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4, Function<T, T5> m5) {
    return new HextupleMapperImpl<>(m0, m1, m2, m3, m4, m5);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6> Function<T, Septuple<T0, T1, T2, T3, T4, T5, T6>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6) {
    return new SeptupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7> Function<T, Octuple<T0, T1, T2, T3, T4, T5, T6, T7>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7) {
    return new OctupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8> Function<T, Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8) {
    return new NonupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> Function<T, Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9) {
    return new DecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Function<T, Undecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10) {
    return new UndecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Function<T, Duodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11) {
    return new DuodecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Function<T, Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12) {
    return new TredecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Function<T, Quattuordecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13) {
    return new QuattuordecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Function<T, Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14) {
    return new QuindecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Function<T, Sexdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15) {
    return new SexdecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Function<T, Septendecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16) {
    return new SeptendecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> Function<T, Octodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17) {
    return new OctodecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> Function<T, Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17, Function<T, T18> m18) {
    return new NovemdecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17,
        m18);
  }

  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> Function<T, Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17, Function<T, T18> m18, Function<T, T19> m19) {
    return new VigintupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17,
        m18, m19);
  }

}
