package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection") final class TuplesTest {

  @Test
  void ofEmptyTuple() {
    assertTuple(Tuples.of(), 0);
  }

  @Test
  void toEmptyTuple() {
    final Function<Integer, EmptyTuple> mapper = Tuples.toTuple();
    assertTuple(mapper.apply(0), 0);
  }

  @Test
  void ofSingle() {
    assertTuple(Tuples.of(0), 1);
  }

  @Test
  void toSingle() {
    final Function<Integer, Single<Integer>> mapper = Tuples.toTuple(i -> i);
    assertTuple(mapper.apply(0), 1);
  }

  @Test
  void ofPair() {
    assertTuple(Tuples.of(0, 1), 2);
  }

  @Test
  void toPair() {
    final Function<Integer, Pair<Integer, Integer>> mapper = Tuples.toTuple(i -> i, i -> i + 1);
    assertTuple(mapper.apply(0), 2);
  }

  @Test
  void ofTriple() {
    assertTuple(Tuples.of(0, 1, 2), 3);
  }

  @Test
  void toTriple() {
    final Function<Integer, Triple<Integer, Integer, Integer>> mapper = Tuples.toTuple(i -> i, i -> i + 1,
        i -> i + 2);
    assertTuple(mapper.apply(0), 3);
  }

  @Test
  void ofQuad() {
    assertTuple(Tuples.of(0, 1, 2, 3), 4);
  }

  @Test
  void toQuad() {
    final Function<Integer, Quad<Integer, Integer, Integer, Integer>> mapper = Tuples.toTuple(i -> i, i -> i + 1,
        i -> i + 2, i -> i + 3);
    assertTuple(mapper.apply(0), 4);
  }

  @Test
  void ofQuintuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4), 5);
  }

  @Test
  void toQuintuple() {
    final Function<Integer, Quintuple<Integer, Integer, Integer, Integer, Integer>> mapper = Tuples.toTuple(i -> i,
        i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4);
    assertTuple(mapper.apply(0), 5);
  }

  @Test
  void ofHextuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5), 6);
  }

  @Test
  void toHextuple() {
    final Function<Integer, Hextuple<Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5);
    assertTuple(mapper.apply(0), 6);
  }

  @Test
  void ofSeptuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6), 7);
  }

  @Test
  void toSeptuple() {
    final Function<Integer, Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6);
    assertTuple(mapper.apply(0), 7);
  }

  @Test
  void ofOctuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7), 8);
  }

  @Test
  void toOctuple() {
    final Function<Integer, Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7);
    assertTuple(mapper.apply(0), 8);
  }

  @Test
  void ofNonuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8), 9);
  }

  @Test
  void toNonuple() {
    final Function<Integer, Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8);
    assertTuple(mapper.apply(0), 9);
  }

  @Test
  void ofDecuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), 10);
  }

  @Test
  void toDecuple() {
    final Function<Integer, Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9);
    assertTuple(mapper.apply(0), 10);
  }

  @Test
  void ofUndecuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 11);
  }

  @Test
  void toUndecuple() {
    final Function<Integer, Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9, i -> i + 10);
    assertTuple(mapper.apply(0), 11);
  }

  @Test
  void ofDuodecuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), 12);
  }

  @Test
  void toDuodecuple() {
    final Function<Integer, Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9, i -> i + 10, i -> i + 11);
    assertTuple(mapper.apply(0), 12);
  }

  @Test
  void ofTredecuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), 13);
  }

  @Test
  void toTredecuple() {
    final Function<Integer, Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9, i -> i + 10, i -> i + 11, i -> i + 12);
    assertTuple(mapper.apply(0), 13);
  }

  @Test
  void ofQuattuordecuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), 14);
  }

  @Test
  void toQuattuordecuple() {
    final Function<Integer, Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9, i -> i + 10, i -> i + 11, i -> i + 12, i -> i + 13);
    assertTuple(mapper.apply(0), 14);
  }

  @Test
  void ofQuindecuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14), 15);
  }

  @Test
  void toQuindecuple() {
    final Function<Integer, Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9, i -> i + 10, i -> i + 11, i -> i + 12, i -> i + 13, i -> i + 14);
    assertTuple(mapper.apply(0), 15);
  }

  @Test
  void ofSexdecuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), 16);
  }

  @Test
  void toSexdecuple() {
    final Function<Integer, Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9, i -> i + 10, i -> i + 11, i -> i + 12, i -> i + 13, i -> i + 14, i -> i + 15);
    assertTuple(mapper.apply(0), 16);
  }

  @Test
  void ofSeptendecuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), 17);
  }

  @Test
  void toSeptendecuple() {
    final Function<Integer, Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9, i -> i + 10, i -> i + 11, i -> i + 12, i -> i + 13, i -> i + 14, i -> i + 15,
            i -> i + 16);
    assertTuple(mapper.apply(0), 17);
  }

  @Test
  void ofOctodecuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17), 18);
  }

  @Test
  void toOctodecuple() {
    final Function<Integer, Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9, i -> i + 10, i -> i + 11, i -> i + 12, i -> i + 13, i -> i + 14, i -> i + 15,
            i -> i + 16, i -> i + 17);
    assertTuple(mapper.apply(0), 18);
  }

  @Test
  void ofNovemdecuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18), 19);
  }

  @Test
  void toNovemdecuple() {
    final Function<Integer, Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9, i -> i + 10, i -> i + 11, i -> i + 12, i -> i + 13, i -> i + 14, i -> i + 15,
            i -> i + 16, i -> i + 17, i -> i + 18);
    assertTuple(mapper.apply(0), 19);
  }

  @Test
  void ofVigintuple() {
    assertTuple(Tuples.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19), 20);
  }

  @Test
  void toVigintuple() {
    final Function<Integer, Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> mapper = Tuples
        .toTuple(i -> i, i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5, i -> i + 6, i -> i + 7,
            i -> i + 8, i -> i + 9, i -> i + 10, i -> i + 11, i -> i + 12, i -> i + 13, i -> i + 14, i -> i + 15,
            i -> i + 16, i -> i + 17, i -> i + 18, i -> i + 19);
    assertTuple(mapper.apply(0), 20);
  }

  @Test
  void testSingleGetters() {
    final Single<Integer> single = Tuples.of(1);
    final FirstAccessor<Single<Integer>, Integer> firstGetter = Single.getFirstGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, firstGetter.apply(single));
  }

  @Test
  void testPairGetters() {
    final Pair<Integer, Integer> pair = Tuples.of(0, 1);
    final FirstAccessor<Pair<Integer, Integer>, Integer> firstGetter = Pair.getFirstGetter();
    final SecondAccessor<Pair<Integer, Integer>, Integer> secondGetter = Pair.getSecondGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(0, firstGetter.apply(pair));
    assertEquals(1, secondGetter.apply(pair));
  }

  @Test
  void testTripleGetters() {
    final Triple<Integer, Integer, String> triple = Tuples.of(0, 1, "Foobar");
    final FirstAccessor<Triple<Integer, Integer, String>, Integer> firstGetter = Triple.getFirstGetter();
    final SecondAccessor<Triple<Integer, Integer, String>, Integer> secondGetter = Triple.getSecondGetter();
    final ThirdAccessor<Triple<Integer, Integer, String>, String> thirdGetter = Triple.getThirdGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(0, firstGetter.apply(triple));
    assertEquals(1, secondGetter.apply(triple));
    assertEquals("Foobar", thirdGetter.apply(triple));
  }

  @Test
  void testQuadGetters() {
    final Quad<Integer, Integer, String, Long> quad = Tuples.of(0, 1, "Foobar", 42L);
    final FirstAccessor<Quad<Integer, Integer, String, Long>, Integer> firstGetter = Quad.getFirstGetter();
    final SecondAccessor<Quad<Integer, Integer, String, Long>, Integer> secondGetter = Quad.getSecondGetter();
    final ThirdAccessor<Quad<Integer, Integer, String, Long>, String> thirdGetter = Quad.getThirdGetter();
    final FourthAccessor<Quad<Integer, Integer, String, Long>, Long> fourthGetter = Quad.getFourthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(0, firstGetter.apply(quad));
    assertEquals(1, secondGetter.apply(quad));
    assertEquals("Foobar", thirdGetter.apply(quad));
    assertEquals(42L, fourthGetter.apply(quad));
  }

  @Test
  void testQuintupleGetters() {
    final Quintuple<Integer, Integer, String, Long, BigInteger> quintuple = Tuples.of(0, 1, "Foobar", 42L,
        BigInteger.ONE);
    final FirstAccessor<Quintuple<Integer, Integer, String, Long, BigInteger>, Integer> firstGetter = Quintuple
        .getFirstGetter();
    final SecondAccessor<Quintuple<Integer, Integer, String, Long, BigInteger>, Integer> secondGetter = Quintuple
        .getSecondGetter();
    final ThirdAccessor<Quintuple<Integer, Integer, String, Long, BigInteger>, String> thirdGetter = Quintuple
        .getThirdGetter();
    final FourthAccessor<Quintuple<Integer, Integer, String, Long, BigInteger>, Long> fourthGetter = Quintuple
        .getFourthGetter();
    final FifthAccessor<Quintuple<Integer, Integer, String, Long, BigInteger>, BigInteger> fifthGetter = Quintuple
        .getFifthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(0, firstGetter.apply(quintuple));
    assertEquals(1, secondGetter.apply(quintuple));
    assertEquals("Foobar", thirdGetter.apply(quintuple));
    assertEquals(42L, fourthGetter.apply(quintuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(quintuple));
  }

  @Test
  void testHextupleGetters() {
    final Hextuple<Integer, Integer, String, Long, BigInteger, String> hextuple = Tuples.of(0, 1, "Foobar", 42L,
        BigInteger.ONE, "qux");
    final FirstAccessor<Hextuple<Integer, Integer, String, Long, BigInteger, String>, Integer> firstGetter = Hextuple
        .getFirstGetter();
    final SecondAccessor<Hextuple<Integer, Integer, String, Long, BigInteger, String>, Integer> secondGetter = Hextuple
        .getSecondGetter();
    final ThirdAccessor<Hextuple<Integer, Integer, String, Long, BigInteger, String>, String> thirdGetter = Hextuple
        .getThirdGetter();
    final FourthAccessor<Hextuple<Integer, Integer, String, Long, BigInteger, String>, Long> fourthGetter = Hextuple
        .getFourthGetter();
    final FifthAccessor<Hextuple<Integer, Integer, String, Long, BigInteger, String>, BigInteger> fifthGetter = Hextuple
        .getFifthGetter();
    final SixthAccessor<Hextuple<Integer, Integer, String, Long, BigInteger, String>, String> sixthGetter = Hextuple
        .getSixthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(0, firstGetter.apply(hextuple));
    assertEquals(1, secondGetter.apply(hextuple));
    assertEquals("Foobar", thirdGetter.apply(hextuple));
    assertEquals(42L, fourthGetter.apply(hextuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(hextuple));
    assertEquals("qux", sixthGetter.apply(hextuple));
  }

  @Test
  void testSeptupleGetters() {
    final Septuple<Integer, Integer, String, Long, BigInteger, String, Integer> septuple = Tuples.of(0, 1, "Foobar",
        42L, BigInteger.ONE, "qux", 20);
    final FirstAccessor<Septuple<Integer, Integer, String, Long, BigInteger, String, Integer>, Integer> firstGetter = Septuple
        .getFirstGetter();
    final SecondAccessor<Septuple<Integer, Integer, String, Long, BigInteger, String, Integer>, Integer> secondGetter = Septuple
        .getSecondGetter();
    final ThirdAccessor<Septuple<Integer, Integer, String, Long, BigInteger, String, Integer>, String> thirdGetter = Septuple
        .getThirdGetter();
    final FourthAccessor<Septuple<Integer, Integer, String, Long, BigInteger, String, Integer>, Long> fourthGetter = Septuple
        .getFourthGetter();
    final FifthAccessor<Septuple<Integer, Integer, String, Long, BigInteger, String, Integer>, BigInteger> fifthGetter = Septuple
        .getFifthGetter();
    final SixthAccessor<Septuple<Integer, Integer, String, Long, BigInteger, String, Integer>, String> sixthGetter = Septuple
        .getSixthGetter();
    final SeventhAccessor<Septuple<Integer, Integer, String, Long, BigInteger, String, Integer>, Integer> seventhGetter = Septuple
        .getSeventhGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(0, firstGetter.apply(septuple));
    assertEquals(1, secondGetter.apply(septuple));
    assertEquals("Foobar", thirdGetter.apply(septuple));
    assertEquals(42L, fourthGetter.apply(septuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(septuple));
    assertEquals("qux", sixthGetter.apply(septuple));
    assertEquals(20, seventhGetter.apply(septuple));
  }

  @Test
  void testOctupleGetters() {
    final Octuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger> octuple = Tuples.of(0, 1,
        "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN);
    final FirstAccessor<Octuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger>, Integer> firstGetter = Octuple
        .getFirstGetter();
    final SecondAccessor<Octuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger>, Integer> secondGetter = Octuple
        .getSecondGetter();
    final ThirdAccessor<Octuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger>, String> thirdGetter = Octuple
        .getThirdGetter();
    final FourthAccessor<Octuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger>, Long> fourthGetter = Octuple
        .getFourthGetter();
    final FifthAccessor<Octuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger>, BigInteger> fifthGetter = Octuple
        .getFifthGetter();
    final SixthAccessor<Octuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger>, String> sixthGetter = Octuple
        .getSixthGetter();
    final SeventhAccessor<Octuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger>, Integer> seventhGetter = Octuple
        .getSeventhGetter();
    final EighthAccessor<Octuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger>, BigInteger> eighthGetter = Octuple
        .getEighthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(0, firstGetter.apply(octuple));
    assertEquals(1, secondGetter.apply(octuple));
    assertEquals("Foobar", thirdGetter.apply(octuple));
    assertEquals(42L, fourthGetter.apply(octuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(octuple));
    assertEquals("qux", sixthGetter.apply(octuple));
    assertEquals(20, seventhGetter.apply(octuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(octuple));
  }

  @Test
  void testNonupleGetters() {
    final Nonuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer> nonuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22);
    final FirstAccessor<Nonuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer>, Integer> firstGetter = Nonuple
        .getFirstGetter();
    final SecondAccessor<Nonuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer>, Integer> secondGetter = Nonuple
        .getSecondGetter();
    final ThirdAccessor<Nonuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer>, String> thirdGetter = Nonuple
        .getThirdGetter();
    final FourthAccessor<Nonuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer>, Long> fourthGetter = Nonuple
        .getFourthGetter();
    final FifthAccessor<Nonuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer>, BigInteger> fifthGetter = Nonuple
        .getFifthGetter();
    final SixthAccessor<Nonuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer>, String> sixthGetter = Nonuple
        .getSixthGetter();
    final SeventhAccessor<Nonuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer>, Integer> seventhGetter = Nonuple
        .getSeventhGetter();
    final EighthAccessor<Nonuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer>, BigInteger> eighthGetter = Nonuple
        .getEighthGetter();
    final NinthAccessor<Nonuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer>, Integer> ninthGetter = Nonuple
        .getNinthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(0, firstGetter.apply(nonuple));
    assertEquals(1, secondGetter.apply(nonuple));
    assertEquals("Foobar", thirdGetter.apply(nonuple));
    assertEquals(42L, fourthGetter.apply(nonuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(nonuple));
    assertEquals("qux", sixthGetter.apply(nonuple));
    assertEquals(20, seventhGetter.apply(nonuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(nonuple));
    assertEquals(22, ninthGetter.apply(nonuple));
  }

  @Test
  void testDecupleGetters() {
    final Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String> decuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge");
    final FirstAccessor<Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String>, Integer> firstGetter = Decuple
        .getFirstGetter();
    final SecondAccessor<Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String>, Integer> secondGetter = Decuple
        .getSecondGetter();
    final ThirdAccessor<Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String>, String> thirdGetter = Decuple
        .getThirdGetter();
    final FourthAccessor<Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String>, Long> fourthGetter = Decuple
        .getFourthGetter();
    final FifthAccessor<Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String>, BigInteger> fifthGetter = Decuple
        .getFifthGetter();
    final SixthAccessor<Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String>, String> sixthGetter = Decuple
        .getSixthGetter();
    final SeventhAccessor<Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String>, Integer> seventhGetter = Decuple
        .getSeventhGetter();
    final EighthAccessor<Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String>, BigInteger> eighthGetter = Decuple
        .getEighthGetter();
    final NinthAccessor<Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String>, Integer> ninthGetter = Decuple
        .getNinthGetter();
    final TenthAccessor<Decuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String>, String> tenthGetter = Decuple
        .getTenthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(0, firstGetter.apply(decuple));
    assertEquals(1, secondGetter.apply(decuple));
    assertEquals("Foobar", thirdGetter.apply(decuple));
    assertEquals(42L, fourthGetter.apply(decuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(decuple));
    assertEquals("qux", sixthGetter.apply(decuple));
    assertEquals(20, seventhGetter.apply(decuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(decuple));
    assertEquals(22, ninthGetter.apply(decuple));
    assertEquals("corge", tenthGetter.apply(decuple));
  }

  @Test
  void testUndecupleGetters() {
    final Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long> undecuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L);
    final FirstAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, Integer> firstGetter = Undecuple
        .getFirstGetter();
    final SecondAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, Integer> secondGetter = Undecuple
        .getSecondGetter();
    final ThirdAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, String> thirdGetter = Undecuple
        .getThirdGetter();
    final FourthAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, Long> fourthGetter = Undecuple
        .getFourthGetter();
    final FifthAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, BigInteger> fifthGetter = Undecuple
        .getFifthGetter();
    final SixthAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, String> sixthGetter = Undecuple
        .getSixthGetter();
    final SeventhAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, Integer> seventhGetter = Undecuple
        .getSeventhGetter();
    final EighthAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, BigInteger> eighthGetter = Undecuple
        .getEighthGetter();
    final NinthAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, Integer> ninthGetter = Undecuple
        .getNinthGetter();
    final TenthAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, String> tenthGetter = Undecuple
        .getTenthGetter();
    final EleventhAccessor<Undecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long>, Long> eleventhGetter = Undecuple
        .getEleventhGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(10, eleventhGetter.index());
    assertEquals(0, firstGetter.apply(undecuple));
    assertEquals(1, secondGetter.apply(undecuple));
    assertEquals("Foobar", thirdGetter.apply(undecuple));
    assertEquals(42L, fourthGetter.apply(undecuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(undecuple));
    assertEquals("qux", sixthGetter.apply(undecuple));
    assertEquals(20, seventhGetter.apply(undecuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(undecuple));
    assertEquals(22, ninthGetter.apply(undecuple));
    assertEquals("corge", tenthGetter.apply(undecuple));
    assertEquals(1L, eleventhGetter.apply(undecuple));
  }

  @Test
  void testDuodecupleGetters() {
    final Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer> duodecuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L, 1);
    final FirstAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, Integer> firstGetter = Duodecuple
        .getFirstGetter();
    final SecondAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, Integer> secondGetter = Duodecuple
        .getSecondGetter();
    final ThirdAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, String> thirdGetter = Duodecuple
        .getThirdGetter();
    final FourthAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, Long> fourthGetter = Duodecuple
        .getFourthGetter();
    final FifthAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, BigInteger> fifthGetter = Duodecuple
        .getFifthGetter();
    final SixthAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, String> sixthGetter = Duodecuple
        .getSixthGetter();
    final SeventhAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, Integer> seventhGetter = Duodecuple
        .getSeventhGetter();
    final EighthAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, BigInteger> eighthGetter = Duodecuple
        .getEighthGetter();
    final NinthAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, Integer> ninthGetter = Duodecuple
        .getNinthGetter();
    final TenthAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, String> tenthGetter = Duodecuple
        .getTenthGetter();
    final EleventhAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, Long> eleventhGetter = Duodecuple
        .getEleventhGetter();
    final TwelfthAccessor<Duodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer>, Integer> twelfthGetter = Duodecuple
        .getTwelfthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(10, eleventhGetter.index());
    assertEquals(11, twelfthGetter.index());
    assertEquals(0, firstGetter.apply(duodecuple));
    assertEquals(1, secondGetter.apply(duodecuple));
    assertEquals("Foobar", thirdGetter.apply(duodecuple));
    assertEquals(42L, fourthGetter.apply(duodecuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(duodecuple));
    assertEquals("qux", sixthGetter.apply(duodecuple));
    assertEquals(20, seventhGetter.apply(duodecuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(duodecuple));
    assertEquals(22, ninthGetter.apply(duodecuple));
    assertEquals("corge", tenthGetter.apply(duodecuple));
    assertEquals(1L, eleventhGetter.apply(duodecuple));
    assertEquals(1, twelfthGetter.apply(duodecuple));
  }

  @Test
  void testTredecupleGetters() {
    final Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String> tredecuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L, 1, "grault");
    final FirstAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, Integer> firstGetter = Tredecuple
        .getFirstGetter();
    final SecondAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, Integer> secondGetter = Tredecuple
        .getSecondGetter();
    final ThirdAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, String> thirdGetter = Tredecuple
        .getThirdGetter();
    final FourthAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, Long> fourthGetter = Tredecuple
        .getFourthGetter();
    final FifthAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, BigInteger> fifthGetter = Tredecuple
        .getFifthGetter();
    final SixthAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, String> sixthGetter = Tredecuple
        .getSixthGetter();
    final SeventhAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, Integer> seventhGetter = Tredecuple
        .getSeventhGetter();
    final EighthAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, BigInteger> eighthGetter = Tredecuple
        .getEighthGetter();
    final NinthAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, Integer> ninthGetter = Tredecuple
        .getNinthGetter();
    final TenthAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, String> tenthGetter = Tredecuple
        .getTenthGetter();
    final EleventhAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, Long> eleventhGetter = Tredecuple
        .getEleventhGetter();
    final TwelfthAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, Integer> twelfthGetter = Tredecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Tredecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String>, String> thirteenthGetter = Tredecuple
        .getThirteenthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(10, eleventhGetter.index());
    assertEquals(11, twelfthGetter.index());
    assertEquals(12, thirteenthGetter.index());
    assertEquals(0, firstGetter.apply(tredecuple));
    assertEquals(1, secondGetter.apply(tredecuple));
    assertEquals("Foobar", thirdGetter.apply(tredecuple));
    assertEquals(42L, fourthGetter.apply(tredecuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(tredecuple));
    assertEquals("qux", sixthGetter.apply(tredecuple));
    assertEquals(20, seventhGetter.apply(tredecuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(tredecuple));
    assertEquals(22, ninthGetter.apply(tredecuple));
    assertEquals("corge", tenthGetter.apply(tredecuple));
    assertEquals(1L, eleventhGetter.apply(tredecuple));
    assertEquals(1, twelfthGetter.apply(tredecuple));
    assertEquals("grault", thirteenthGetter.apply(tredecuple));
  }

  @Test
  void testQuattuordecupleGetters() {
    final Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer> quattuordecuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L, 1, "grault", 2);
    final FirstAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, Integer> firstGetter = Quattuordecuple
        .getFirstGetter();
    final SecondAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, Integer> secondGetter = Quattuordecuple
        .getSecondGetter();
    final ThirdAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, String> thirdGetter = Quattuordecuple
        .getThirdGetter();
    final FourthAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, Long> fourthGetter = Quattuordecuple
        .getFourthGetter();
    final FifthAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, BigInteger> fifthGetter = Quattuordecuple
        .getFifthGetter();
    final SixthAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, String> sixthGetter = Quattuordecuple
        .getSixthGetter();
    final SeventhAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, Integer> seventhGetter = Quattuordecuple
        .getSeventhGetter();
    final EighthAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, BigInteger> eighthGetter = Quattuordecuple
        .getEighthGetter();
    final NinthAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, Integer> ninthGetter = Quattuordecuple
        .getNinthGetter();
    final TenthAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, String> tenthGetter = Quattuordecuple
        .getTenthGetter();
    final EleventhAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, Long> eleventhGetter = Quattuordecuple
        .getEleventhGetter();
    final TwelfthAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, Integer> twelfthGetter = Quattuordecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, String> thirteenthGetter = Quattuordecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Quattuordecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer>, Integer> fourteenthGetter = Quattuordecuple
        .getFourteenthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(10, eleventhGetter.index());
    assertEquals(11, twelfthGetter.index());
    assertEquals(12, thirteenthGetter.index());
    assertEquals(13, fourteenthGetter.index());
    assertEquals(0, firstGetter.apply(quattuordecuple));
    assertEquals(1, secondGetter.apply(quattuordecuple));
    assertEquals("Foobar", thirdGetter.apply(quattuordecuple));
    assertEquals(42L, fourthGetter.apply(quattuordecuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(quattuordecuple));
    assertEquals("qux", sixthGetter.apply(quattuordecuple));
    assertEquals(20, seventhGetter.apply(quattuordecuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(quattuordecuple));
    assertEquals(22, ninthGetter.apply(quattuordecuple));
    assertEquals("corge", tenthGetter.apply(quattuordecuple));
    assertEquals(1L, eleventhGetter.apply(quattuordecuple));
    assertEquals(1, twelfthGetter.apply(quattuordecuple));
    assertEquals("grault", thirteenthGetter.apply(quattuordecuple));
    assertEquals(2, fourteenthGetter.apply(quattuordecuple));
  }

  @Test
  void testQuindecupleGetters() {
    final Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String> quindecuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L, 1, "grault", 2, "garply");
    final FirstAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, Integer> firstGetter = Quindecuple
        .getFirstGetter();
    final SecondAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, Integer> secondGetter = Quindecuple
        .getSecondGetter();
    final ThirdAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, String> thirdGetter = Quindecuple
        .getThirdGetter();
    final FourthAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, Long> fourthGetter = Quindecuple
        .getFourthGetter();
    final FifthAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, BigInteger> fifthGetter = Quindecuple
        .getFifthGetter();
    final SixthAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, String> sixthGetter = Quindecuple
        .getSixthGetter();
    final SeventhAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, Integer> seventhGetter = Quindecuple
        .getSeventhGetter();
    final EighthAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, BigInteger> eighthGetter = Quindecuple
        .getEighthGetter();
    final NinthAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, Integer> ninthGetter = Quindecuple
        .getNinthGetter();
    final TenthAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, String> tenthGetter = Quindecuple
        .getTenthGetter();
    final EleventhAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, Long> eleventhGetter = Quindecuple
        .getEleventhGetter();
    final TwelfthAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, Integer> twelfthGetter = Quindecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, String> thirteenthGetter = Quindecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, Integer> fourteenthGetter = Quindecuple
        .getFourteenthGetter();
    final FifteenthAccessor<Quindecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String>, String> fifteenthGetter = Quindecuple
        .getFifteenthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(10, eleventhGetter.index());
    assertEquals(11, twelfthGetter.index());
    assertEquals(12, thirteenthGetter.index());
    assertEquals(13, fourteenthGetter.index());
    assertEquals(14, fifteenthGetter.index());
    assertEquals(0, firstGetter.apply(quindecuple));
    assertEquals(1, secondGetter.apply(quindecuple));
    assertEquals("Foobar", thirdGetter.apply(quindecuple));
    assertEquals(42L, fourthGetter.apply(quindecuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(quindecuple));
    assertEquals("qux", sixthGetter.apply(quindecuple));
    assertEquals(20, seventhGetter.apply(quindecuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(quindecuple));
    assertEquals(22, ninthGetter.apply(quindecuple));
    assertEquals("corge", tenthGetter.apply(quindecuple));
    assertEquals(1L, eleventhGetter.apply(quindecuple));
    assertEquals(1, twelfthGetter.apply(quindecuple));
    assertEquals("grault", thirteenthGetter.apply(quindecuple));
    assertEquals(2, fourteenthGetter.apply(quindecuple));
    assertEquals("garply", fifteenthGetter.apply(quindecuple));
  }

  @Test
  void testSexdecupleGetters() {
    final Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer> sexdecuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L, 1, "grault", 2, "garply",
            3);
    final FirstAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, Integer> firstGetter = Sexdecuple
        .getFirstGetter();
    final SecondAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, Integer> secondGetter = Sexdecuple
        .getSecondGetter();
    final ThirdAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, String> thirdGetter = Sexdecuple
        .getThirdGetter();
    final FourthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, Long> fourthGetter = Sexdecuple
        .getFourthGetter();
    final FifthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, BigInteger> fifthGetter = Sexdecuple
        .getFifthGetter();
    final SixthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, String> sixthGetter = Sexdecuple
        .getSixthGetter();
    final SeventhAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, Integer> seventhGetter = Sexdecuple
        .getSeventhGetter();
    final EighthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, BigInteger> eighthGetter = Sexdecuple
        .getEighthGetter();
    final NinthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, Integer> ninthGetter = Sexdecuple
        .getNinthGetter();
    final TenthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, String> tenthGetter = Sexdecuple
        .getTenthGetter();
    final EleventhAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, Long> eleventhGetter = Sexdecuple
        .getEleventhGetter();
    final TwelfthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, Integer> twelfthGetter = Sexdecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, String> thirteenthGetter = Sexdecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, Integer> fourteenthGetter = Sexdecuple
        .getFourteenthGetter();
    final FifteenthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, String> fifteenthGetter = Sexdecuple
        .getFifteenthGetter();
    final SixteenthAccessor<Sexdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer>, Integer> sixteenthGetter = Sexdecuple
        .getSixteenthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(10, eleventhGetter.index());
    assertEquals(11, twelfthGetter.index());
    assertEquals(12, thirteenthGetter.index());
    assertEquals(13, fourteenthGetter.index());
    assertEquals(14, fifteenthGetter.index());
    assertEquals(15, sixteenthGetter.index());
    assertEquals(0, firstGetter.apply(sexdecuple));
    assertEquals(1, secondGetter.apply(sexdecuple));
    assertEquals("Foobar", thirdGetter.apply(sexdecuple));
    assertEquals(42L, fourthGetter.apply(sexdecuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(sexdecuple));
    assertEquals("qux", sixthGetter.apply(sexdecuple));
    assertEquals(20, seventhGetter.apply(sexdecuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(sexdecuple));
    assertEquals(22, ninthGetter.apply(sexdecuple));
    assertEquals("corge", tenthGetter.apply(sexdecuple));
    assertEquals(1L, eleventhGetter.apply(sexdecuple));
    assertEquals(1, twelfthGetter.apply(sexdecuple));
    assertEquals("grault", thirteenthGetter.apply(sexdecuple));
    assertEquals(2, fourteenthGetter.apply(sexdecuple));
    assertEquals("garply", fifteenthGetter.apply(sexdecuple));
    assertEquals(3, sixteenthGetter.apply(sexdecuple));
  }

  @Test
  void testSeptendecupleGetters() {
    final Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String> septendecuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L, 1, "grault", 2, "garply",
            3, "waldo");
    final FirstAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, Integer> firstGetter = Septendecuple
        .getFirstGetter();
    final SecondAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, Integer> secondGetter = Septendecuple
        .getSecondGetter();
    final ThirdAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, String> thirdGetter = Septendecuple
        .getThirdGetter();
    final FourthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, Long> fourthGetter = Septendecuple
        .getFourthGetter();
    final FifthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, BigInteger> fifthGetter = Septendecuple
        .getFifthGetter();
    final SixthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, String> sixthGetter = Septendecuple
        .getSixthGetter();
    final SeventhAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, Integer> seventhGetter = Septendecuple
        .getSeventhGetter();
    final EighthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, BigInteger> eighthGetter = Septendecuple
        .getEighthGetter();
    final NinthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, Integer> ninthGetter = Septendecuple
        .getNinthGetter();
    final TenthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, String> tenthGetter = Septendecuple
        .getTenthGetter();
    final EleventhAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, Long> eleventhGetter = Septendecuple
        .getEleventhGetter();
    final TwelfthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, Integer> twelfthGetter = Septendecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, String> thirteenthGetter = Septendecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, Integer> fourteenthGetter = Septendecuple
        .getFourteenthGetter();
    final FifteenthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, String> fifteenthGetter = Septendecuple
        .getFifteenthGetter();
    final SixteenthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, Integer> sixteenthGetter = Septendecuple
        .getSixteenthGetter();
    final SeventeenthAccessor<Septendecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String>, String> seventeenthGetter = Septendecuple
        .getSeventeenthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(10, eleventhGetter.index());
    assertEquals(11, twelfthGetter.index());
    assertEquals(12, thirteenthGetter.index());
    assertEquals(13, fourteenthGetter.index());
    assertEquals(14, fifteenthGetter.index());
    assertEquals(15, sixteenthGetter.index());
    assertEquals(16, seventeenthGetter.index());
    assertEquals(0, firstGetter.apply(septendecuple));
    assertEquals(1, secondGetter.apply(septendecuple));
    assertEquals("Foobar", thirdGetter.apply(septendecuple));
    assertEquals(42L, fourthGetter.apply(septendecuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(septendecuple));
    assertEquals("qux", sixthGetter.apply(septendecuple));
    assertEquals(20, seventhGetter.apply(septendecuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(septendecuple));
    assertEquals(22, ninthGetter.apply(septendecuple));
    assertEquals("corge", tenthGetter.apply(septendecuple));
    assertEquals(1L, eleventhGetter.apply(septendecuple));
    assertEquals(1, twelfthGetter.apply(septendecuple));
    assertEquals("grault", thirteenthGetter.apply(septendecuple));
    assertEquals(2, fourteenthGetter.apply(septendecuple));
    assertEquals("garply", fifteenthGetter.apply(septendecuple));
    assertEquals(3, sixteenthGetter.apply(septendecuple));
    assertEquals("waldo", seventeenthGetter.apply(septendecuple));
  }

  @Test
  void testOctodecupleGetters() {
    final Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer> octodecuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L, 1, "grault", 2, "garply",
            3, "waldo", 4);
    final FirstAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, Integer> firstGetter = Octodecuple
        .getFirstGetter();
    final SecondAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, Integer> secondGetter = Octodecuple
        .getSecondGetter();
    final ThirdAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, String> thirdGetter = Octodecuple
        .getThirdGetter();
    final FourthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, Long> fourthGetter = Octodecuple
        .getFourthGetter();
    final FifthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, BigInteger> fifthGetter = Octodecuple
        .getFifthGetter();
    final SixthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, String> sixthGetter = Octodecuple
        .getSixthGetter();
    final SeventhAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, Integer> seventhGetter = Octodecuple
        .getSeventhGetter();
    final EighthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, BigInteger> eighthGetter = Octodecuple
        .getEighthGetter();
    final NinthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, Integer> ninthGetter = Octodecuple
        .getNinthGetter();
    final TenthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, String> tenthGetter = Octodecuple
        .getTenthGetter();
    final EleventhAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, Long> eleventhGetter = Octodecuple
        .getEleventhGetter();
    final TwelfthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, Integer> twelfthGetter = Octodecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, String> thirteenthGetter = Octodecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, Integer> fourteenthGetter = Octodecuple
        .getFourteenthGetter();
    final FifteenthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, String> fifteenthGetter = Octodecuple
        .getFifteenthGetter();
    final SixteenthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, Integer> sixteenthGetter = Octodecuple
        .getSixteenthGetter();
    final SeventeenthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, String> seventeenthGetter = Octodecuple
        .getSeventeenthGetter();
    final EighteenthAccessor<Octodecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer>, Integer> eighteenthGetter = Octodecuple
        .getEighteenthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(10, eleventhGetter.index());
    assertEquals(11, twelfthGetter.index());
    assertEquals(12, thirteenthGetter.index());
    assertEquals(13, fourteenthGetter.index());
    assertEquals(14, fifteenthGetter.index());
    assertEquals(15, sixteenthGetter.index());
    assertEquals(16, seventeenthGetter.index());
    assertEquals(17, eighteenthGetter.index());
    assertEquals(0, firstGetter.apply(octodecuple));
    assertEquals(1, secondGetter.apply(octodecuple));
    assertEquals("Foobar", thirdGetter.apply(octodecuple));
    assertEquals(42L, fourthGetter.apply(octodecuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(octodecuple));
    assertEquals("qux", sixthGetter.apply(octodecuple));
    assertEquals(20, seventhGetter.apply(octodecuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(octodecuple));
    assertEquals(22, ninthGetter.apply(octodecuple));
    assertEquals("corge", tenthGetter.apply(octodecuple));
    assertEquals(1L, eleventhGetter.apply(octodecuple));
    assertEquals(1, twelfthGetter.apply(octodecuple));
    assertEquals("grault", thirteenthGetter.apply(octodecuple));
    assertEquals(2, fourteenthGetter.apply(octodecuple));
    assertEquals("garply", fifteenthGetter.apply(octodecuple));
    assertEquals(3, sixteenthGetter.apply(octodecuple));
    assertEquals("waldo", seventeenthGetter.apply(octodecuple));
    assertEquals(4, eighteenthGetter.apply(octodecuple));
  }

  @Test
  void testNovemdecupleGetters() {
    final Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String> novemdecuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L, 1, "grault", 2, "garply",
            3, "waldo", 4, "fred");
    final FirstAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, Integer> firstGetter = Novemdecuple
        .getFirstGetter();
    final SecondAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, Integer> secondGetter = Novemdecuple
        .getSecondGetter();
    final ThirdAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, String> thirdGetter = Novemdecuple
        .getThirdGetter();
    final FourthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, Long> fourthGetter = Novemdecuple
        .getFourthGetter();
    final FifthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, BigInteger> fifthGetter = Novemdecuple
        .getFifthGetter();
    final SixthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, String> sixthGetter = Novemdecuple
        .getSixthGetter();
    final SeventhAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, Integer> seventhGetter = Novemdecuple
        .getSeventhGetter();
    final EighthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, BigInteger> eighthGetter = Novemdecuple
        .getEighthGetter();
    final NinthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, Integer> ninthGetter = Novemdecuple
        .getNinthGetter();
    final TenthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, String> tenthGetter = Novemdecuple
        .getTenthGetter();
    final EleventhAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, Long> eleventhGetter = Novemdecuple
        .getEleventhGetter();
    final TwelfthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, Integer> twelfthGetter = Novemdecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, String> thirteenthGetter = Novemdecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, Integer> fourteenthGetter = Novemdecuple
        .getFourteenthGetter();
    final FifteenthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, String> fifteenthGetter = Novemdecuple
        .getFifteenthGetter();
    final SixteenthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, Integer> sixteenthGetter = Novemdecuple
        .getSixteenthGetter();
    final SeventeenthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, String> seventeenthGetter = Novemdecuple
        .getSeventeenthGetter();
    final EighteenthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, Integer> eighteenthGetter = Novemdecuple
        .getEighteenthGetter();
    final NineteenthAccessor<Novemdecuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String>, String> nineteenthGetter = Novemdecuple
        .getNineteenthGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(10, eleventhGetter.index());
    assertEquals(11, twelfthGetter.index());
    assertEquals(12, thirteenthGetter.index());
    assertEquals(13, fourteenthGetter.index());
    assertEquals(14, fifteenthGetter.index());
    assertEquals(15, sixteenthGetter.index());
    assertEquals(16, seventeenthGetter.index());
    assertEquals(17, eighteenthGetter.index());
    assertEquals(18, nineteenthGetter.index());
    assertEquals(0, firstGetter.apply(novemdecuple));
    assertEquals(1, secondGetter.apply(novemdecuple));
    assertEquals("Foobar", thirdGetter.apply(novemdecuple));
    assertEquals(42L, fourthGetter.apply(novemdecuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(novemdecuple));
    assertEquals("qux", sixthGetter.apply(novemdecuple));
    assertEquals(20, seventhGetter.apply(novemdecuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(novemdecuple));
    assertEquals(22, ninthGetter.apply(novemdecuple));
    assertEquals("corge", tenthGetter.apply(novemdecuple));
    assertEquals(1L, eleventhGetter.apply(novemdecuple));
    assertEquals(1, twelfthGetter.apply(novemdecuple));
    assertEquals("grault", thirteenthGetter.apply(novemdecuple));
    assertEquals(2, fourteenthGetter.apply(novemdecuple));
    assertEquals("garply", fifteenthGetter.apply(novemdecuple));
    assertEquals(3, sixteenthGetter.apply(novemdecuple));
    assertEquals("waldo", seventeenthGetter.apply(novemdecuple));
    assertEquals(4, eighteenthGetter.apply(novemdecuple));
    assertEquals("fred", nineteenthGetter.apply(novemdecuple));
  }

  @Test
  void testVigintupleGetters() {
    final Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer> vigintuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L, 1, "grault", 2, "garply",
            3, "waldo", 4, "fred", 5);
    final FirstAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Integer> firstGetter = Vigintuple
        .getFirstGetter();
    final SecondAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Integer> secondGetter = Vigintuple
        .getSecondGetter();
    final ThirdAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, String> thirdGetter = Vigintuple
        .getThirdGetter();
    final FourthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Long> fourthGetter = Vigintuple
        .getFourthGetter();
    final FifthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, BigInteger> fifthGetter = Vigintuple
        .getFifthGetter();
    final SixthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, String> sixthGetter = Vigintuple
        .getSixthGetter();
    final SeventhAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Integer> seventhGetter = Vigintuple
        .getSeventhGetter();
    final EighthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, BigInteger> eighthGetter = Vigintuple
        .getEighthGetter();
    final NinthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Integer> ninthGetter = Vigintuple
        .getNinthGetter();
    final TenthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, String> tenthGetter = Vigintuple
        .getTenthGetter();
    final EleventhAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Long> eleventhGetter = Vigintuple
        .getEleventhGetter();
    final TwelfthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Integer> twelfthGetter = Vigintuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, String> thirteenthGetter = Vigintuple
        .getThirteenthGetter();
    final FourteenthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Integer> fourteenthGetter = Vigintuple
        .getFourteenthGetter();
    final FifteenthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, String> fifteenthGetter = Vigintuple
        .getFifteenthGetter();
    final SixteenthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Integer> sixteenthGetter = Vigintuple
        .getSixteenthGetter();
    final SeventeenthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, String> seventeenthGetter = Vigintuple
        .getSeventeenthGetter();
    final EighteenthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Integer> eighteenthGetter = Vigintuple
        .getEighteenthGetter();
    final NineteenthAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, String> nineteenthGetter = Vigintuple
        .getNineteenthGetter();
    final TwentiethAccessor<Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer>, Integer> twentiethGetter = Vigintuple
        .getTwentiethGetter();
    assertEquals(0, firstGetter.index());
    assertEquals(1, secondGetter.index());
    assertEquals(2, thirdGetter.index());
    assertEquals(3, fourthGetter.index());
    assertEquals(4, fifthGetter.index());
    assertEquals(5, sixthGetter.index());
    assertEquals(6, seventhGetter.index());
    assertEquals(7, eighthGetter.index());
    assertEquals(8, ninthGetter.index());
    assertEquals(9, tenthGetter.index());
    assertEquals(10, eleventhGetter.index());
    assertEquals(11, twelfthGetter.index());
    assertEquals(12, thirteenthGetter.index());
    assertEquals(13, fourteenthGetter.index());
    assertEquals(14, fifteenthGetter.index());
    assertEquals(15, sixteenthGetter.index());
    assertEquals(16, seventeenthGetter.index());
    assertEquals(17, eighteenthGetter.index());
    assertEquals(18, nineteenthGetter.index());
    assertEquals(19, twentiethGetter.index());
    assertEquals(0, firstGetter.apply(vigintuple));
    assertEquals(1, secondGetter.apply(vigintuple));
    assertEquals("Foobar", thirdGetter.apply(vigintuple));
    assertEquals(42L, fourthGetter.apply(vigintuple));
    assertEquals(BigInteger.ONE, fifthGetter.apply(vigintuple));
    assertEquals("qux", sixthGetter.apply(vigintuple));
    assertEquals(20, seventhGetter.apply(vigintuple));
    assertEquals(BigInteger.TEN, eighthGetter.apply(vigintuple));
    assertEquals(22, ninthGetter.apply(vigintuple));
    assertEquals("corge", tenthGetter.apply(vigintuple));
    assertEquals(1L, eleventhGetter.apply(vigintuple));
    assertEquals(1, twelfthGetter.apply(vigintuple));
    assertEquals("grault", thirteenthGetter.apply(vigintuple));
    assertEquals(2, fourteenthGetter.apply(vigintuple));
    assertEquals("garply", fifteenthGetter.apply(vigintuple));
    assertEquals(3, sixteenthGetter.apply(vigintuple));
    assertEquals("waldo", seventeenthGetter.apply(vigintuple));
    assertEquals(4, eighteenthGetter.apply(vigintuple));
    assertEquals("fred", nineteenthGetter.apply(vigintuple));
    assertEquals(5, twentiethGetter.apply(vigintuple));
  }

  @Test
  void testOfArray() {
    final Random random = new Random();
    final Map<Integer, Object[]> tupleData = new HashMap<>();
    IntStream.range(0, 22).forEach(i -> {
      Integer[] array = new Integer[i];
      for (int j = 0; j < i; j++) {
        array[j] = random.nextInt();
      }
      tupleData.put(i, array);
    });

    Map<Integer, Tuple> tuplesFromArrays = new HashMap<>();
    tupleData.forEach((k, v) -> tuplesFromArrays.put(k, Tuples.ofArray(new String[] {}, v)));

    tuplesFromArrays.forEach((k, tuple) -> {
      Object[] parts = tuple.stream().toArray();

      assertThat(parts).containsExactly(tupleData.get(k));
      assertThat(tuple.size()).isEqualTo(k);
    });
  }
  
  @Test
  void testEmptyTuple() {
    final EmptyTuple empty = Tuples.of();
    assertThat(empty.size()).isZero();
    
    IndexOutOfBoundsException exception = Assertions.assertThrows(IndexOutOfBoundsException.class, () -> empty.get(0));

    String expectedErrorMessage = "index 0 is illegal. The degree of this Tuple is 0.";
    Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
  }

  private void assertTuple(Tuple tuple, int degree) {
    assertEquals(degree, tuple.size());
    for (int i = 0; i < degree; i++) {
      assertEquals(i, tuple.get(i));
    }
  }
}