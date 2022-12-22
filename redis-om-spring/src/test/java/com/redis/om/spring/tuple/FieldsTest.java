package com.redis.om.spring.tuple;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpellCheckingInspection") class FieldsTest {
  @Test
  void testFieldsOf() {
    Function<Integer, Integer> f = (Integer i) -> i * 2;

    Function<Integer, EmptyTuple> fEmpty = Fields.of();
    Function<Integer, Single<Integer>> fSingle = Fields.of(f);
    Function<Integer, Pair<Integer, Integer>> fPair = Fields.of(f, f);
    Function<Integer, Triple<Integer, Integer, Integer>> fTriple = Fields.of(f, f, f);
    Function<Integer, Quad<Integer, Integer, Integer, Integer>> fQuad = Fields.of(f, f, f, f);
    Function<Integer, Quintuple<Integer, Integer, Integer, Integer, Integer>> fQuintuple = Fields.of(f, f, f, f, f);
    Function<Integer, Hextuple< //
        Integer, Integer, Integer, Integer, Integer, Integer> //
    > fHextuple = Fields.of(f, f, f, f, f, f);
    Function<Integer, Septuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, Integer> //
    > fSeptuple = Fields.of(f, f, f, f, f, f, f);
    Function<Integer, Octuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> //
    > fOctuple = Fields.of(f, f, f, f, f, f, f, f);
    Function<Integer, Nonuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> //
    > fNonuple = Fields.of(f, f, f, f, f, f, f, f, f);
    Function<Integer, Decuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> //
    > fDecuple = Fields.of(f, f, f, f, f, f, f, f, f, f);
    Function<Integer, Undecuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> //
    > fUndecuple = Fields.of(f, f, f, f, f, f, f, f, f, f, f);
    Function<Integer, Duodecuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer> //
    > fDuodecuple = Fields.of(f, f, f, f, f, f, f, f, f, f, f, f);
    Function<Integer, Tredecuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer> //
    > fTredecuple = Fields.of(f, f, f, f, f, f, f, f, f, f, f, f, f);
    Function<Integer, Quattuordecuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer> //
    > fQuattuordecuple = Fields.of(f, f, f, f, f, f, f, f, f, f, f, f, f, f);
    Function<Integer, Quindecuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer> //
    > fQuindecuple = Fields.of(f, f, f, f, f, f, f, f, f, f, f, f, f, f, f);
    Function<Integer, Sexdecuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer> //
    > fSexdecuple = Fields.of(f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f);
    Function<Integer, Septendecuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer> //
    > fSeptendecuple = Fields.of(f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f);
    Function<Integer, Octodecuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer> //
    > fOctodecuple = Fields.of(f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f);
    Function<Integer, Novemdecuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer, Integer> //
    > fNovemdecuple = Fields.of(f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f);
    Function<Integer, Vigintuple< //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer, //
        Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> //
    > fVigintuple = Fields.of(f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f, f);

    assertThat(fEmpty.apply(0).streamOf(Integer.class)).isEmpty();
    assertThat(fSingle.apply(2).streamOf(Integer.class)).containsExactly(4);
    assertThat(fPair.apply(3).streamOf(Integer.class)).containsSequence(6, 6);
    assertThat(fTriple.apply(4).streamOf(Integer.class)).containsExactly(8, 8, 8);
    assertThat(fQuad.apply(5).streamOf(Integer.class)).containsExactly(10, 10, 10, 10);
    assertThat(fQuintuple.apply(6).streamOf(Integer.class)).containsExactly(12, 12, 12, 12, 12);
    assertThat(fHextuple.apply(7).streamOf(Integer.class)).containsExactly(14, 14, 14, 14, 14, 14);
    assertThat(fSeptuple.apply(8).streamOf(Integer.class)).containsExactly(16, 16, 16, 16, 16, 16, 16);
    assertThat(fOctuple.apply(9).streamOf(Integer.class)).containsExactly(18, 18, 18, 18, 18, 18, 18, 18);
    assertThat(fNonuple.apply(10).streamOf(Integer.class)).containsExactly(20, 20, 20, 20, 20, 20, 20, 20, 20);
    assertThat(fDecuple.apply(11).streamOf(Integer.class)).containsExactly(22, 22, 22, 22, 22, 22, 22, 22, 22, 22);
    assertThat(fUndecuple.apply(12).streamOf(Integer.class)).containsExactly(24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24);
    assertThat(fDuodecuple.apply(13).streamOf(Integer.class)).containsExactly(26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
        26, 26);
    assertThat(fTredecuple.apply(14).streamOf(Integer.class)).containsExactly(28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
        28, 28, 28);
    assertThat(fQuattuordecuple.apply(15).streamOf(Integer.class)).containsExactly(30, 30, 30, 30, 30, 30, 30, 30, 30,
        30, 30, 30, 30, 30);
    assertThat(fQuindecuple.apply(16).streamOf(Integer.class)).containsExactly(32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32);
    assertThat(fSexdecuple.apply(17).streamOf(Integer.class)).containsExactly(34, 34, 34, 34, 34, 34, 34, 34, 34, 34,
        34, 34, 34, 34, 34, 34);
    assertThat(fSeptendecuple.apply(18).streamOf(Integer.class)).containsExactly(36, 36, 36, 36, 36, 36, 36, 36, 36, 36,
        36, 36, 36, 36, 36, 36, 36);
    assertThat(fOctodecuple.apply(19).streamOf(Integer.class)).containsExactly(38, 38, 38, 38, 38, 38, 38, 38, 38, 38,
        38, 38, 38, 38, 38, 38, 38, 38);
    assertThat(fNovemdecuple.apply(20).streamOf(Integer.class)).containsExactly(40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
        40, 40, 40, 40, 40, 40, 40, 40, 40);
    assertThat(fVigintuple.apply(21).streamOf(Integer.class)).containsExactly(42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
        42, 42, 42, 42, 42, 42, 42, 42, 42, 42);
  }
}
