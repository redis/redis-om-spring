package com.redis.om.spring.tuple;

public class TupleBuilder {

  private Tuple current;

  private TupleBuilder() {
    this.current = Tuples.of();
  }

  public static EmptyTupleBuilder builder() {
    return new TupleBuilder().new EmptyTupleBuilder();
  }

  public class EmptyTupleBuilder extends BaseBuilder<EmptyTuple> {

    public <T0> SingleBuilder<T0> add(T0 e0) {
      current = Tuples.of(e0);
      return new SingleBuilder<>();
    }
  }

  public class SingleBuilder<T0> extends BaseBuilder<Single<T0>> {

    public <T1> PairBuilder<T0, T1> add(T1 e1) {
      current = Tuples.of(current.get(0), e1);
      return new PairBuilder<>();
    }
  }

  public class PairBuilder<T0, T1> extends BaseBuilder<Pair<T0, T1>> {

    public <T2> TripleBuilder<T0, T1, T2> add(T2 e2) {
      current = Tuples.of(current.get(0), current.get(1), e2);
      return new TripleBuilder<>();
    }
  }

  public class TripleBuilder<T0, T1, T2> extends BaseBuilder<Triple<T0, T1, T2>> {

    public <T3> QuadBuilder<T0, T1, T2, T3> add(T3 e3) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), e3);
      return new QuadBuilder<>();
    }
  }

  public class QuadBuilder<T0, T1, T2, T3> extends BaseBuilder<Quad<T0, T1, T2, T3>> {

    public <T4> QuintupleBuilder<T0, T1, T2, T3, T4> add(T4 e4) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), e4);
      return new QuintupleBuilder<>();
    }
  }

  public class QuintupleBuilder<T0, T1, T2, T3, T4> extends BaseBuilder<Quintuple<T0, T1, T2, T3, T4>> {

    public <T5> HextupleBuilder<T0, T1, T2, T3, T4, T5> add(T5 e5) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4), e5);
      return new HextupleBuilder<>();
    }
  }

  public class HextupleBuilder<T0, T1, T2, T3, T4, T5> extends BaseBuilder<Hextuple<T0, T1, T2, T3, T4, T5>> {

    public <T6> SeptupleBuilder<T0, T1, T2, T3, T4, T5, T6> add(T6 e6) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), e6);
      return new SeptupleBuilder<>();
    }
  }

  public class SeptupleBuilder<T0, T1, T2, T3, T4, T5, T6> extends BaseBuilder<Septuple<T0, T1, T2, T3, T4, T5, T6>> {

    public <T7> OctupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7> add(T7 e7) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), e7);
      return new OctupleBuilder<>();
    }
  }

  public class OctupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7> extends BaseBuilder<Octuple<T0, T1, T2, T3, T4, T5, T6, T7>> {

    public <T8> NonupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8> add(T8 e8) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), e8);
      return new NonupleBuilder<>();
    }
  }

  public class NonupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8>
      extends BaseBuilder<Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8>> {

    public <T9> DecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> add(T9 e9) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), e9);
      return new DecupleBuilder<>();
    }
  }

  public class DecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>
      extends BaseBuilder<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>> {

    public <T10> UndecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> add(T10 e10) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), current.get(9), e10);
      return new UndecupleBuilder<>();
    }
  }

  public class UndecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>
      extends BaseBuilder<Undecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> {

    public <T11> DuodecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> add(T11 e11) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), current.get(9), current.get(10), e11);
      return new DuodecupleBuilder<>();
    }
  }

  public class DuodecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>
      extends BaseBuilder<Duodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> {

    public <T12> TredecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> add(T12 e12) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), current.get(9), current.get(10),
          current.get(11), e12);
      return new TredecupleBuilder<>();
    }
  }

  public class TredecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>
      extends BaseBuilder<Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> {

    public <T13> QuattuordecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> add(T13 e13) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), current.get(9), current.get(10),
          current.get(11), current.get(12), e13);
      return new QuattuordecupleBuilder<>();
    }
  }

  public class QuattuordecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>
      extends BaseBuilder<Quattuordecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> {

    public <T14> QuindecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> add(T14 e14) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), current.get(9), current.get(10),
          current.get(11), current.get(12), current.get(13), e14);
      return new QuindecupleBuilder<>();
    }
  }

  public class QuindecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
      extends BaseBuilder<Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> {

    public <T15> SexdecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> add(T15 e15) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), current.get(9), current.get(10),
          current.get(11), current.get(12), current.get(13), current.get(14), e15);
      return new SexdecupleBuilder<>();
    }
  }

  public class SexdecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
      extends BaseBuilder<Sexdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> {

    public <T16> SeptendecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> add(T16 e16) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), current.get(9), current.get(10),
          current.get(11), current.get(12), current.get(13), current.get(14), current.get(15), e16);
      return new SeptendecupleBuilder<>();
    }
  }

  public class SeptendecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>
      extends BaseBuilder<Septendecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> {

    public <T17> OctodecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> add(
        T17 e17) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), current.get(9), current.get(10),
          current.get(11), current.get(12), current.get(13), current.get(14), current.get(15), current.get(16), e17);
      return new OctodecupleBuilder<>();
    }
  }

  public class OctodecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>
      extends BaseBuilder<Octodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>> {

    public <T18> NovemdecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> add(
        T18 e18) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), current.get(9), current.get(10),
          current.get(11), current.get(12), current.get(13), current.get(14), current.get(15), current.get(16),
          current.get(17), e18);
      return new NovemdecupleBuilder<>();
    }
  }

  public class NovemdecupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> extends
      BaseBuilder<Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>> {

    public <T19> VigintupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> add(
        T19 e19) {
      current = Tuples.of(current.get(0), current.get(1), current.get(2), current.get(3), current.get(4),
          current.get(5), current.get(6), current.get(7), current.get(8), current.get(9), current.get(10),
          current.get(11), current.get(12), current.get(13), current.get(14), current.get(15), current.get(16),
          current.get(17), current.get(18), e19);
      return new VigintupleBuilder<>();
    }
  }

  public class VigintupleBuilder<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>
      extends
      BaseBuilder<Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>> {
  }

  private class BaseBuilder<T> {

    @SuppressWarnings("unchecked")
    public T build() {
      return (T) current;
    }
  }
}