package com.redis.om.spring.tuple;

import java.util.function.Function;

import com.redis.om.spring.tuple.impl.*;
import com.redis.om.spring.tuple.impl.mapper.*;

/**
 * Factory class for creating tuples of various sizes.
 * 
 * <p>This class provides static factory methods for creating tuples ranging from
 * empty tuples to tuples containing up to 20 elements. For tuples with more than
 * 20 elements, a variable-degree tuple implementation is used.</p>
 * 
 * <p>The class offers two main categories of factory methods:</p>
 * <ul>
 * <li><b>Creation methods:</b> {@code of(...)} methods for creating tuples with specific elements</li>
 * <li><b>Mapping methods:</b> {@code toTuple(...)} methods for creating functions that extract tuple elements from
 * objects</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * // Create a pair
 * Pair<String, Integer> pair = Tuples.of("Hello", 42);
 * 
 * // Create a triple with labels
 * Triple<String, Integer, Double> triple = Tuples.of(
 * new String[]{"name", "age", "score"},
 * "John", 30, 95.5
 * );
 * 
 * // Create a mapping function
 * Function<Person, Pair<String, Integer>> mapper = Tuples.toTuple(
 * Person::getName,
 * Person::getAge
 * );
 * }</pre>
 */
public final class Tuples {

  private Tuples() {
  }

  /**
   * Creates an empty tuple containing no elements.
   * 
   * @return an empty tuple
   */
  public static EmptyTuple of() {
    return EmptyTupleImpl.EMPTY_TUPLE;
  }

  /**
   * Creates a single-element tuple.
   * 
   * @param <T0> the type of the element
   * @param e0   the element
   * @return a single containing the element
   */
  public static <T0> Single<T0> of(T0 e0) {
    return new SingleImpl<>(new String[] {}, e0);
  }

  /**
   * Creates a single-element tuple with labels.
   * 
   * @param <T0>   the type of the element
   * @param labels the labels for the tuple elements
   * @param e0     the element
   * @return a single containing the element with associated labels
   */
  public static <T0> Single<T0> of(String[] labels, T0 e0) {
    return new SingleImpl<>(labels, e0);
  }

  /**
   * Creates a pair (2-element tuple).
   * 
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param e0   the first element
   * @param e1   the second element
   * @return a pair containing the two elements
   */
  public static <T0, T1> Pair<T0, T1> of(T0 e0, T1 e1) {
    return new PairImpl<>(new String[] {}, e0, e1);
  }

  /**
   * Creates a pair (2-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param labels the labels for the tuple elements
   * @param e0     the first element
   * @param e1     the second element
   * @return a pair containing the two elements with associated labels
   */
  public static <T0, T1> Pair<T0, T1> of(String[] labels, T0 e0, T1 e1) {
    return new PairImpl<>(labels, e0, e1);
  }

  /**
   * Creates a triple (3-element tuple).
   * 
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param e0   the first element
   * @param e1   the second element
   * @param e2   the third element
   * @return a triple containing the three elements
   */
  public static <T0, T1, T2> Triple<T0, T1, T2> of(T0 e0, T1 e1, T2 e2) {
    return new TripleImpl<>(new String[] {}, e0, e1, e2);
  }

  /**
   * Creates a triple (3-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param labels the labels for the tuple elements
   * @param e0     the first element
   * @param e1     the second element
   * @param e2     the third element
   * @return a triple containing the three elements with associated labels
   */
  public static <T0, T1, T2> Triple<T0, T1, T2> of(String[] labels, T0 e0, T1 e1, T2 e2) {
    return new TripleImpl<>(labels, e0, e1, e2);
  }

  /**
   * Creates a quad (4-element tuple).
   * 
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param e0   the first element
   * @param e1   the second element
   * @param e2   the third element
   * @param e3   the fourth element
   * @return a quad containing the four elements
   */
  public static <T0, T1, T2, T3> Quad<T0, T1, T2, T3> of(T0 e0, T1 e1, T2 e2, T3 e3) {
    return new QuadImpl<>(new String[] {}, e0, e1, e2, e3);
  }

  /**
   * Creates a quad (4-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param labels the labels for the tuple elements
   * @param e0     the first element
   * @param e1     the second element
   * @param e2     the third element
   * @param e3     the fourth element
   * @return a quad containing the four elements with associated labels
   */
  public static <T0, T1, T2, T3> Quad<T0, T1, T2, T3> of(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3) {
    return new QuadImpl<>(labels, e0, e1, e2, e3);
  }

  /**
   * Creates a quintuple (5-element tuple).
   * 
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param e0   the first element
   * @param e1   the second element
   * @param e2   the third element
   * @param e3   the fourth element
   * @param e4   the fifth element
   * @return a quintuple containing the five elements
   */
  public static <T0, T1, T2, T3, T4> Quintuple<T0, T1, T2, T3, T4> of(T0 e0, T1 e1, T2 e2, T3 e3, T4 e4) {
    return new QuintupleImpl<>(new String[] {}, e0, e1, e2, e3, e4);
  }

  /**
   * Creates a quintuple (5-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param labels the labels for the tuple elements
   * @param e0     the first element
   * @param e1     the second element
   * @param e2     the third element
   * @param e3     the fourth element
   * @param e4     the fifth element
   * @return a quintuple containing the five elements with associated labels
   */
  public static <T0, T1, T2, T3, T4> Quintuple<T0, T1, T2, T3, T4> of(String[] labels, T0 e0, T1 e1, T2 e2, T3 e3,
      T4 e4) {
    return new QuintupleImpl<>(labels, e0, e1, e2, e3, e4);
  }

  /**
   * Creates a hextuple (6-element tuple).
   * 
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param e0   the first element
   * @param e1   the second element
   * @param e2   the third element
   * @param e3   the fourth element
   * @param e4   the fifth element
   * @param e5   the sixth element
   * @return a hextuple containing the six elements
   */
  public static <T0, T1, T2, T3, T4, T5> Hextuple<T0, T1, T2, T3, T4, T5> of(T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5) {
    return new HextupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5);
  }

  /**
   * Creates a hextuple (6-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param labels the labels for the tuple elements
   * @param e0     the first element
   * @param e1     the second element
   * @param e2     the third element
   * @param e3     the fourth element
   * @param e4     the fifth element
   * @param e5     the sixth element
   * @return a hextuple containing the six elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5> Hextuple<T0, T1, T2, T3, T4, T5> of(String[] labels, T0 e0, T1 e1, T2 e2,
      T3 e3, T4 e4, T5 e5) {
    return new HextupleImpl<>(labels, e0, e1, e2, e3, e4, e5);
  }

  /**
   * Creates a septuple (7-element tuple).
   * 
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param e0   the first element
   * @param e1   the second element
   * @param e2   the third element
   * @param e3   the fourth element
   * @param e4   the fifth element
   * @param e5   the sixth element
   * @param e6   the seventh element
   * @return a septuple containing the seven elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6> Septuple<T0, T1, T2, T3, T4, T5, T6> of(T0 e0, T1 e1, T2 e2, T3 e3, T4 e4,
      T5 e5, T6 e6) {
    return new SeptupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6);
  }

  /**
   * Creates a septuple (7-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param labels the labels for the tuple elements
   * @param e0     the first element
   * @param e1     the second element
   * @param e2     the third element
   * @param e3     the fourth element
   * @param e4     the fifth element
   * @param e5     the sixth element
   * @param e6     the seventh element
   * @return a septuple containing the seven elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6> Septuple<T0, T1, T2, T3, T4, T5, T6> of(String[] labels, T0 e0, T1 e1,
      T2 e2, T3 e3, T4 e4, T5 e5, T6 e6) {
    return new SeptupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6);
  }

  /**
   * Creates an octuple (8-element tuple).
   * 
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param e0   the first element
   * @param e1   the second element
   * @param e2   the third element
   * @param e3   the fourth element
   * @param e4   the fifth element
   * @param e5   the sixth element
   * @param e6   the seventh element
   * @param e7   the eighth element
   * @return an octuple containing the eight elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7> Octuple<T0, T1, T2, T3, T4, T5, T6, T7> of(T0 e0, T1 e1, T2 e2, T3 e3,
      T4 e4, T5 e5, T6 e6, T7 e7) {
    return new OctupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7);
  }

  /**
   * Creates an octuple (8-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param labels the labels for the tuple elements
   * @param e0     the first element
   * @param e1     the second element
   * @param e2     the third element
   * @param e3     the fourth element
   * @param e4     the fifth element
   * @param e5     the sixth element
   * @param e6     the seventh element
   * @param e7     the eighth element
   * @return an octuple containing the eight elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7> Octuple<T0, T1, T2, T3, T4, T5, T6, T7> of(String[] labels, T0 e0,
      T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7) {
    return new OctupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7);
  }

  /**
   * Creates a nonuple (9-element tuple).
   * 
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param e0   the first element
   * @param e1   the second element
   * @param e2   the third element
   * @param e3   the fourth element
   * @param e4   the fifth element
   * @param e5   the sixth element
   * @param e6   the seventh element
   * @param e7   the eighth element
   * @param e8   the ninth element
   * @return a nonuple containing the nine elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8> Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8> of(T0 e0, T1 e1, T2 e2,
      T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8) {
    return new NonupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8);
  }

  /**
   * Creates a nonuple (9-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
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
   * @return a nonuple containing the nine elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8> Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8> of(String[] labels,
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8) {
    return new NonupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8);
  }

  /**
   * Creates a decuple (10-element tuple).
   * 
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @param e0   the first element
   * @param e1   the second element
   * @param e2   the third element
   * @param e3   the fourth element
   * @param e4   the fifth element
   * @param e5   the sixth element
   * @param e6   the seventh element
   * @param e7   the eighth element
   * @param e8   the ninth element
   * @param e9   the tenth element
   * @return a decuple containing the ten elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T0 e0,
      T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9) {
    return new DecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9);
  }

  /**
   * Creates a decuple (10-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
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
   * @return a decuple containing the ten elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9) {
    return new DecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9);
  }

  /**
   * Creates an undecuple (11-element tuple).
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
   * @param e0    the first element
   * @param e1    the second element
   * @param e2    the third element
   * @param e3    the fourth element
   * @param e4    the fifth element
   * @param e5    the sixth element
   * @param e6    the seventh element
   * @param e7    the eighth element
   * @param e8    the ninth element
   * @param e9    the tenth element
   * @param e10   the eleventh element
   * @return an undecuple containing the eleven elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Undecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10) {
    return new UndecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
  }

  /**
   * Creates an undecuple (11-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
   * @param <T10>  the type of the eleventh element
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
   * @return an undecuple containing the eleven elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Undecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10) {
    return new UndecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
  }

  /**
   * Creates a duodecuple (12-element tuple).
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
   * @param e0    the first element
   * @param e1    the second element
   * @param e2    the third element
   * @param e3    the fourth element
   * @param e4    the fifth element
   * @param e5    the sixth element
   * @param e6    the seventh element
   * @param e7    the eighth element
   * @param e8    the ninth element
   * @param e9    the tenth element
   * @param e10   the eleventh element
   * @param e11   the twelfth element
   * @return a duodecuple containing the twelve elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Duodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11) {
    return new DuodecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11);
  }

  /**
   * Creates a duodecuple (12-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
   * @param <T10>  the type of the eleventh element
   * @param <T11>  the type of the twelfth element
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
   * @return a duodecuple containing the twelve elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Duodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11) {
    return new DuodecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11);
  }

  /**
   * Creates a tredecuple (13-element tuple).
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
   * @param e0    the first element
   * @param e1    the second element
   * @param e2    the third element
   * @param e3    the fourth element
   * @param e4    the fifth element
   * @param e5    the sixth element
   * @param e6    the seventh element
   * @param e7    the eighth element
   * @param e8    the ninth element
   * @param e9    the tenth element
   * @param e10   the eleventh element
   * @param e11   the twelfth element
   * @param e12   the thirteenth element
   * @return a tredecuple containing the thirteen elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12) {
    return new TredecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12);
  }

  /**
   * Creates a tredecuple (13-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
   * @param <T10>  the type of the eleventh element
   * @param <T11>  the type of the twelfth element
   * @param <T12>  the type of the thirteenth element
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
   * @return a tredecuple containing the thirteen elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11,
      T12 e12) {
    return new TredecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12);
  }

  /**
   * Creates a quattuordecuple (14-element tuple).
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
   * @param e0    the first element
   * @param e1    the second element
   * @param e2    the third element
   * @param e3    the fourth element
   * @param e4    the fifth element
   * @param e5    the sixth element
   * @param e6    the seventh element
   * @param e7    the eighth element
   * @param e8    the ninth element
   * @param e9    the tenth element
   * @param e10   the eleventh element
   * @param e11   the twelfth element
   * @param e12   the thirteenth element
   * @param e13   the fourteenth element
   * @return a quattuordecuple containing the fourteen elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Quattuordecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13) {
    return new QuattuordecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13);
  }

  /**
   * Creates a quattuordecuple (14-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
   * @param <T10>  the type of the eleventh element
   * @param <T11>  the type of the twelfth element
   * @param <T12>  the type of the thirteenth element
   * @param <T13>  the type of the fourteenth element
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
   * @return a quattuordecuple containing the fourteen elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Quattuordecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12,
      T13 e13) {
    return new QuattuordecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13);
  }

  /**
   * Creates a quindecuple (15-element tuple).
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
   * @param e0    the first element
   * @param e1    the second element
   * @param e2    the third element
   * @param e3    the fourth element
   * @param e4    the fifth element
   * @param e5    the sixth element
   * @param e6    the seventh element
   * @param e7    the eighth element
   * @param e8    the ninth element
   * @param e9    the tenth element
   * @param e10   the eleventh element
   * @param e11   the twelfth element
   * @param e12   the thirteenth element
   * @param e13   the fourteenth element
   * @param e14   the fifteenth element
   * @return a quindecuple containing the fifteen elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13,
      T14 e14) {
    return new QuindecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14);
  }

  /**
   * Creates a quindecuple (15-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
   * @param <T10>  the type of the eleventh element
   * @param <T11>  the type of the twelfth element
   * @param <T12>  the type of the thirteenth element
   * @param <T13>  the type of the fourteenth element
   * @param <T14>  the type of the fifteenth element
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
   * @return a quindecuple containing the fifteen elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12,
      T13 e13, T14 e14) {
    return new QuindecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14);
  }

  /**
   * Creates a sexdecuple (16-element tuple).
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
   * @param e0    the first element
   * @param e1    the second element
   * @param e2    the third element
   * @param e3    the fourth element
   * @param e4    the fifth element
   * @param e5    the sixth element
   * @param e6    the seventh element
   * @param e7    the eighth element
   * @param e8    the ninth element
   * @param e9    the tenth element
   * @param e10   the eleventh element
   * @param e11   the twelfth element
   * @param e12   the thirteenth element
   * @param e13   the fourteenth element
   * @param e14   the fifteenth element
   * @param e15   the sixteenth element
   * @return a sexdecuple containing the sixteen elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Sexdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15) {
    return new SexdecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15);
  }

  /**
   * Creates a sexdecuple (16-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
   * @param <T10>  the type of the eleventh element
   * @param <T11>  the type of the twelfth element
   * @param <T12>  the type of the thirteenth element
   * @param <T13>  the type of the fourteenth element
   * @param <T14>  the type of the fifteenth element
   * @param <T15>  the type of the sixteenth element
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
   * @return a sexdecuple containing the sixteen elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Sexdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12,
      T13 e13, T14 e14, T15 e15) {
    return new SexdecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15);
  }

  /**
   * Creates a septendecuple (17-element tuple).
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
   * @param e0    the first element
   * @param e1    the second element
   * @param e2    the third element
   * @param e3    the fourth element
   * @param e4    the fifth element
   * @param e5    the sixth element
   * @param e6    the seventh element
   * @param e7    the eighth element
   * @param e8    the ninth element
   * @param e9    the tenth element
   * @param e10   the eleventh element
   * @param e11   the twelfth element
   * @param e12   the thirteenth element
   * @param e13   the fourteenth element
   * @param e14   the fifteenth element
   * @param e15   the sixteenth element
   * @param e16   the seventeenth element
   * @return a septendecuple containing the seventeen elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Septendecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16) {
    return new SeptendecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14,
        e15, e16);
  }

  /**
   * Creates a septendecuple (17-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
   * @param <T10>  the type of the eleventh element
   * @param <T11>  the type of the twelfth element
   * @param <T12>  the type of the thirteenth element
   * @param <T13>  the type of the fourteenth element
   * @param <T14>  the type of the fifteenth element
   * @param <T15>  the type of the sixteenth element
   * @param <T16>  the type of the seventeenth element
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
   * @return a septendecuple containing the seventeen elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Septendecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12,
      T13 e13, T14 e14, T15 e15, T16 e16) {
    return new SeptendecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16);
  }

  /**
   * Creates an octodecuple (18-element tuple).
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
   * @param e0    the first element
   * @param e1    the second element
   * @param e2    the third element
   * @param e3    the fourth element
   * @param e4    the fifth element
   * @param e5    the sixth element
   * @param e6    the seventh element
   * @param e7    the eighth element
   * @param e8    the ninth element
   * @param e9    the tenth element
   * @param e10   the eleventh element
   * @param e11   the twelfth element
   * @param e12   the thirteenth element
   * @param e13   the fourteenth element
   * @param e14   the fifteenth element
   * @param e15   the sixteenth element
   * @param e16   the seventeenth element
   * @param e17   the eighteenth element
   * @return an octodecuple containing the eighteen elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> Octodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17) {
    return new OctodecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15,
        e16, e17);
  }

  /**
   * Creates an octodecuple (18-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
   * @param <T10>  the type of the eleventh element
   * @param <T11>  the type of the twelfth element
   * @param <T12>  the type of the thirteenth element
   * @param <T13>  the type of the fourteenth element
   * @param <T14>  the type of the fifteenth element
   * @param <T15>  the type of the sixteenth element
   * @param <T16>  the type of the seventeenth element
   * @param <T17>  the type of the eighteenth element
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
   * @return an octodecuple containing the eighteen elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> Octodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12,
      T13 e13, T14 e14, T15 e15, T16 e16, T17 e17) {
    return new OctodecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17);
  }

  /**
   * Creates a novemdecuple (19-element tuple).
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
   * @param e0    the first element
   * @param e1    the second element
   * @param e2    the third element
   * @param e3    the fourth element
   * @param e4    the fifth element
   * @param e5    the sixth element
   * @param e6    the seventh element
   * @param e7    the eighth element
   * @param e8    the ninth element
   * @param e9    the tenth element
   * @param e10   the eleventh element
   * @param e11   the twelfth element
   * @param e12   the thirteenth element
   * @param e13   the fourteenth element
   * @param e14   the fifteenth element
   * @param e15   the sixteenth element
   * @param e16   the seventeenth element
   * @param e17   the eighteenth element
   * @param e18   the nineteenth element
   * @return a novemdecuple containing the nineteen elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17, T18 e18) {
    return new NovemdecupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15,
        e16, e17, e18);
  }

  /**
   * Creates a novemdecuple (19-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
   * @param <T10>  the type of the eleventh element
   * @param <T11>  the type of the twelfth element
   * @param <T12>  the type of the thirteenth element
   * @param <T13>  the type of the fourteenth element
   * @param <T14>  the type of the fifteenth element
   * @param <T15>  the type of the sixteenth element
   * @param <T16>  the type of the seventeenth element
   * @param <T17>  the type of the eighteenth element
   * @param <T18>  the type of the nineteenth element
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
   * @return a novemdecuple containing the nineteen elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12,
      T13 e13, T14 e14, T15 e15, T16 e16, T17 e17, T18 e18) {
    return new NovemdecupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17, e18);
  }

  /**
   * Creates a vigintuple (20-element tuple).
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
   * @param e0    the first element
   * @param e1    the second element
   * @param e2    the third element
   * @param e3    the fourth element
   * @param e4    the fifth element
   * @param e5    the sixth element
   * @param e6    the seventh element
   * @param e7    the eighth element
   * @param e8    the ninth element
   * @param e9    the tenth element
   * @param e10   the eleventh element
   * @param e11   the twelfth element
   * @param e12   the thirteenth element
   * @param e13   the fourteenth element
   * @param e14   the fifteenth element
   * @param e15   the sixteenth element
   * @param e16   the seventeenth element
   * @param e17   the eighteenth element
   * @param e18   the nineteenth element
   * @param e19   the twentieth element
   * @return a vigintuple containing the twenty elements
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> of(
      T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12, T13 e13, T14 e14,
      T15 e15, T16 e16, T17 e17, T18 e18, T19 e19) {
    return new VigintupleImpl<>(new String[] {}, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15,
        e16, e17, e18, e19);
  }

  /**
   * Creates a vigintuple (20-element tuple) with labels.
   * 
   * @param <T0>   the type of the first element
   * @param <T1>   the type of the second element
   * @param <T2>   the type of the third element
   * @param <T3>   the type of the fourth element
   * @param <T4>   the type of the fifth element
   * @param <T5>   the type of the sixth element
   * @param <T6>   the type of the seventh element
   * @param <T7>   the type of the eighth element
   * @param <T8>   the type of the ninth element
   * @param <T9>   the type of the tenth element
   * @param <T10>  the type of the eleventh element
   * @param <T11>  the type of the twelfth element
   * @param <T12>  the type of the thirteenth element
   * @param <T13>  the type of the fourteenth element
   * @param <T14>  the type of the fifteenth element
   * @param <T15>  the type of the sixteenth element
   * @param <T16>  the type of the seventeenth element
   * @param <T17>  the type of the eighteenth element
   * @param <T18>  the type of the nineteenth element
   * @param <T19>  the type of the twentieth element
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
   * @return a vigintuple containing the twenty elements with associated labels
   */
  public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> of(
      String[] labels, T0 e0, T1 e1, T2 e2, T3 e3, T4 e4, T5 e5, T6 e6, T7 e7, T8 e8, T9 e9, T10 e10, T11 e11, T12 e12,
      T13 e13, T14 e14, T15 e15, T16 e16, T17 e17, T18 e18, T19 e19) {
    return new VigintupleImpl<>(labels, e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17,
        e18, e19);
  }

  /**
   * Creates a tuple from an array of elements.
   * 
   * <p>This method dynamically creates the appropriate tuple type based on the
   * number of elements provided. For arrays with 1-20 elements, it creates
   * the corresponding fixed-degree tuple. For arrays with more than 20 elements,
   * it creates a variable-degree tuple.</p>
   * 
   * @param returnFields the field labels for the tuple elements
   * @param el           the elements to include in the tuple
   * @return a tuple containing all the provided elements
   */
  @SafeVarargs
  public static Tuple ofArray(String[] returnFields, Object... el) {
    return switch (el.length) {
      case 0 -> of();
      case 1 -> of(returnFields, el[0]);
      case 2 -> of(returnFields, el[0], el[1]);
      case 3 -> of(returnFields, el[0], el[1], el[2]);
      case 4 -> of(returnFields, el[0], el[1], el[2], el[3]);
      case 5 -> of(returnFields, el[0], el[1], el[2], el[3], el[4]);
      case 6 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5]);
      case 7 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6]);
      case 8 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7]);
      case 9 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8]);
      case 10 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9]);
      case 11 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10]);
      case 12 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11]);
      case 13 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11],
          el[12]);
      case 14 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11],
          el[12], el[13]);
      case 15 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11],
          el[12], el[13], el[14]);
      case 16 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11],
          el[12], el[13], el[14], el[15]);
      case 17 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11],
          el[12], el[13], el[14], el[15], el[16]);
      case 18 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11],
          el[12], el[13], el[14], el[15], el[16], el[17]);
      case 19 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11],
          el[12], el[13], el[14], el[15], el[16], el[17], el[18]);
      case 20 -> of(returnFields, el[0], el[1], el[2], el[3], el[4], el[5], el[6], el[7], el[8], el[9], el[10], el[11],
          el[12], el[13], el[14], el[15], el[16], el[17], el[18], el[19]);
      default -> new TupleInfiniteDegreeImpl(el);
    };
  }

  /**
   * Creates a function that maps objects to empty tuples.
   * 
   * @param <T> the type of objects to map
   * @return a function that produces empty tuples
   */
  @SuppressWarnings(
    "unchecked"
  )
  public static <T> Function<T, EmptyTuple> toTuple() {
    return (Function<T, EmptyTuple>) EmptyTupleMapperImpl.EMPTY_MAPPER;
  }

  /**
   * Creates a function that maps objects to single-element tuples.
   * 
   * @param <T>  the type of objects to map
   * @param <T0> the type of the element
   * @param m0   the mapping function for extracting the element
   * @return a function that produces singles
   */
  public static <T, T0> Function<T, Single<T0>> toTuple(Function<T, T0> m0) {
    return new SingleMapperImpl<>(m0);
  }

  /**
   * Creates a function that maps objects to pairs.
   * 
   * @param <T>  the type of objects to map
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param m0   the mapping function for extracting the first element
   * @param m1   the mapping function for extracting the second element
   * @return a function that produces pairs
   */
  public static <T, T0, T1> Function<T, Pair<T0, T1>> toTuple(Function<T, T0> m0, Function<T, T1> m1) {
    return new PairMapperImpl<>(m0, m1);
  }

  /**
   * Creates a function that maps objects to triples.
   * 
   * @param <T>  the type of objects to map
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param m0   the mapping function for extracting the first element
   * @param m1   the mapping function for extracting the second element
   * @param m2   the mapping function for extracting the third element
   * @return a function that produces triples
   */
  public static <T, T0, T1, T2> Function<T, Triple<T0, T1, T2>> toTuple(Function<T, T0> m0, Function<T, T1> m1,
      Function<T, T2> m2) {
    return new TripleMapperImpl<>(m0, m1, m2);
  }

  /**
   * Creates a function that maps objects to quads.
   * 
   * @param <T>  the type of objects to map
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param m0   the mapping function for extracting the first element
   * @param m1   the mapping function for extracting the second element
   * @param m2   the mapping function for extracting the third element
   * @param m3   the mapping function for extracting the fourth element
   * @return a function that produces quads
   */
  public static <T, T0, T1, T2, T3> Function<T, Quad<T0, T1, T2, T3>> toTuple(Function<T, T0> m0, Function<T, T1> m1,
      Function<T, T2> m2, Function<T, T3> m3) {
    return new QuadMapperImpl<>(m0, m1, m2, m3);
  }

  /**
   * Creates a function that maps objects to quintuples.
   * 
   * @param <T>  the type of objects to map
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param m0   the mapping function for extracting the first element
   * @param m1   the mapping function for extracting the second element
   * @param m2   the mapping function for extracting the third element
   * @param m3   the mapping function for extracting the fourth element
   * @param m4   the mapping function for extracting the fifth element
   * @return a function that produces quintuples
   */
  public static <T, T0, T1, T2, T3, T4> Function<T, Quintuple<T0, T1, T2, T3, T4>> toTuple(Function<T, T0> m0,
      Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4) {
    return new QuintupleMapperImpl<>(m0, m1, m2, m3, m4);
  }

  /**
   * Creates a function that maps objects to hextuples.
   * 
   * @param <T>  the type of objects to map
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param m0   the mapping function for extracting the first element
   * @param m1   the mapping function for extracting the second element
   * @param m2   the mapping function for extracting the third element
   * @param m3   the mapping function for extracting the fourth element
   * @param m4   the mapping function for extracting the fifth element
   * @param m5   the mapping function for extracting the sixth element
   * @return a function that produces hextuples
   */
  public static <T, T0, T1, T2, T3, T4, T5> Function<T, Hextuple<T0, T1, T2, T3, T4, T5>> toTuple(Function<T, T0> m0,
      Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4, Function<T, T5> m5) {
    return new HextupleMapperImpl<>(m0, m1, m2, m3, m4, m5);
  }

  /**
   * Creates a function that maps objects to septuples.
   * 
   * @param <T>  the type of objects to map
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param m0   the mapping function for extracting the first element
   * @param m1   the mapping function for extracting the second element
   * @param m2   the mapping function for extracting the third element
   * @param m3   the mapping function for extracting the fourth element
   * @param m4   the mapping function for extracting the fifth element
   * @param m5   the mapping function for extracting the sixth element
   * @param m6   the mapping function for extracting the seventh element
   * @return a function that produces septuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6> Function<T, Septuple<T0, T1, T2, T3, T4, T5, T6>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6) {
    return new SeptupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6);
  }

  /**
   * Creates a function that maps objects to octuples.
   * 
   * @param <T>  the type of objects to map
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param m0   the mapping function for extracting the first element
   * @param m1   the mapping function for extracting the second element
   * @param m2   the mapping function for extracting the third element
   * @param m3   the mapping function for extracting the fourth element
   * @param m4   the mapping function for extracting the fifth element
   * @param m5   the mapping function for extracting the sixth element
   * @param m6   the mapping function for extracting the seventh element
   * @param m7   the mapping function for extracting the eighth element
   * @return a function that produces octuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7> Function<T, Octuple<T0, T1, T2, T3, T4, T5, T6, T7>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7) {
    return new OctupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7);
  }

  /**
   * Creates a function that maps objects to nonuples.
   * 
   * @param <T>  the type of objects to map
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param m0   the mapping function for extracting the first element
   * @param m1   the mapping function for extracting the second element
   * @param m2   the mapping function for extracting the third element
   * @param m3   the mapping function for extracting the fourth element
   * @param m4   the mapping function for extracting the fifth element
   * @param m5   the mapping function for extracting the sixth element
   * @param m6   the mapping function for extracting the seventh element
   * @param m7   the mapping function for extracting the eighth element
   * @param m8   the mapping function for extracting the ninth element
   * @return a function that produces nonuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8> Function<T, Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8) {
    return new NonupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8);
  }

  /**
   * Creates a function that maps objects to decuples.
   * 
   * @param <T>  the type of objects to map
   * @param <T0> the type of the first element
   * @param <T1> the type of the second element
   * @param <T2> the type of the third element
   * @param <T3> the type of the fourth element
   * @param <T4> the type of the fifth element
   * @param <T5> the type of the sixth element
   * @param <T6> the type of the seventh element
   * @param <T7> the type of the eighth element
   * @param <T8> the type of the ninth element
   * @param <T9> the type of the tenth element
   * @param m0   the mapping function for extracting the first element
   * @param m1   the mapping function for extracting the second element
   * @param m2   the mapping function for extracting the third element
   * @param m3   the mapping function for extracting the fourth element
   * @param m4   the mapping function for extracting the fifth element
   * @param m5   the mapping function for extracting the sixth element
   * @param m6   the mapping function for extracting the seventh element
   * @param m7   the mapping function for extracting the eighth element
   * @param m8   the mapping function for extracting the ninth element
   * @param m9   the mapping function for extracting the tenth element
   * @return a function that produces decuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> Function<T, Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9) {
    return new DecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9);
  }

  /**
   * Creates a function that maps objects to undecuples.
   * 
   * @param <T>   the type of objects to map
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
   * @param m0    the mapping function for extracting the first element
   * @param m1    the mapping function for extracting the second element
   * @param m2    the mapping function for extracting the third element
   * @param m3    the mapping function for extracting the fourth element
   * @param m4    the mapping function for extracting the fifth element
   * @param m5    the mapping function for extracting the sixth element
   * @param m6    the mapping function for extracting the seventh element
   * @param m7    the mapping function for extracting the eighth element
   * @param m8    the mapping function for extracting the ninth element
   * @param m9    the mapping function for extracting the tenth element
   * @param m10   the mapping function for extracting the eleventh element
   * @return a function that produces undecuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Function<T, Undecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10) {
    return new UndecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10);
  }

  /**
   * Creates a function that maps objects to duodecuples.
   * 
   * @param <T>   the type of objects to map
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
   * @param m0    the mapping function for extracting the first element
   * @param m1    the mapping function for extracting the second element
   * @param m2    the mapping function for extracting the third element
   * @param m3    the mapping function for extracting the fourth element
   * @param m4    the mapping function for extracting the fifth element
   * @param m5    the mapping function for extracting the sixth element
   * @param m6    the mapping function for extracting the seventh element
   * @param m7    the mapping function for extracting the eighth element
   * @param m8    the mapping function for extracting the ninth element
   * @param m9    the mapping function for extracting the tenth element
   * @param m10   the mapping function for extracting the eleventh element
   * @param m11   the mapping function for extracting the twelfth element
   * @return a function that produces duodecuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Function<T, Duodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11) {
    return new DuodecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11);
  }

  /**
   * Creates a function that maps objects to tredecuples.
   * 
   * @param <T>   the type of objects to map
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
   * @param m0    the mapping function for extracting the first element
   * @param m1    the mapping function for extracting the second element
   * @param m2    the mapping function for extracting the third element
   * @param m3    the mapping function for extracting the fourth element
   * @param m4    the mapping function for extracting the fifth element
   * @param m5    the mapping function for extracting the sixth element
   * @param m6    the mapping function for extracting the seventh element
   * @param m7    the mapping function for extracting the eighth element
   * @param m8    the mapping function for extracting the ninth element
   * @param m9    the mapping function for extracting the tenth element
   * @param m10   the mapping function for extracting the eleventh element
   * @param m11   the mapping function for extracting the twelfth element
   * @param m12   the mapping function for extracting the thirteenth element
   * @return a function that produces tredecuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Function<T, Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12) {
    return new TredecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12);
  }

  /**
   * Creates a function that maps objects to quattuordecuples.
   * 
   * @param <T>   the type of objects to map
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
   * @param m0    the mapping function for extracting the first element
   * @param m1    the mapping function for extracting the second element
   * @param m2    the mapping function for extracting the third element
   * @param m3    the mapping function for extracting the fourth element
   * @param m4    the mapping function for extracting the fifth element
   * @param m5    the mapping function for extracting the sixth element
   * @param m6    the mapping function for extracting the seventh element
   * @param m7    the mapping function for extracting the eighth element
   * @param m8    the mapping function for extracting the ninth element
   * @param m9    the mapping function for extracting the tenth element
   * @param m10   the mapping function for extracting the eleventh element
   * @param m11   the mapping function for extracting the twelfth element
   * @param m12   the mapping function for extracting the thirteenth element
   * @param m13   the mapping function for extracting the fourteenth element
   * @return a function that produces quattuordecuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Function<T, Quattuordecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13) {
    return new QuattuordecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13);
  }

  /**
   * Creates a function that maps objects to quindecuples.
   * 
   * @param <T>   the type of objects to map
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
   * @param m0    the mapping function for extracting the first element
   * @param m1    the mapping function for extracting the second element
   * @param m2    the mapping function for extracting the third element
   * @param m3    the mapping function for extracting the fourth element
   * @param m4    the mapping function for extracting the fifth element
   * @param m5    the mapping function for extracting the sixth element
   * @param m6    the mapping function for extracting the seventh element
   * @param m7    the mapping function for extracting the eighth element
   * @param m8    the mapping function for extracting the ninth element
   * @param m9    the mapping function for extracting the tenth element
   * @param m10   the mapping function for extracting the eleventh element
   * @param m11   the mapping function for extracting the twelfth element
   * @param m12   the mapping function for extracting the thirteenth element
   * @param m13   the mapping function for extracting the fourteenth element
   * @param m14   the mapping function for extracting the fifteenth element
   * @return a function that produces quindecuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Function<T, Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14) {
    return new QuindecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14);
  }

  /**
   * Creates a function that maps objects to sexdecuples.
   * 
   * @param <T>   the type of objects to map
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
   * @param m0    the mapping function for extracting the first element
   * @param m1    the mapping function for extracting the second element
   * @param m2    the mapping function for extracting the third element
   * @param m3    the mapping function for extracting the fourth element
   * @param m4    the mapping function for extracting the fifth element
   * @param m5    the mapping function for extracting the sixth element
   * @param m6    the mapping function for extracting the seventh element
   * @param m7    the mapping function for extracting the eighth element
   * @param m8    the mapping function for extracting the ninth element
   * @param m9    the mapping function for extracting the tenth element
   * @param m10   the mapping function for extracting the eleventh element
   * @param m11   the mapping function for extracting the twelfth element
   * @param m12   the mapping function for extracting the thirteenth element
   * @param m13   the mapping function for extracting the fourteenth element
   * @param m14   the mapping function for extracting the fifteenth element
   * @param m15   the mapping function for extracting the sixteenth element
   * @return a function that produces sexdecuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Function<T, Sexdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15) {
    return new SexdecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15);
  }

  /**
   * Creates a function that maps objects to septendecuples.
   * 
   * @param <T>   the type of objects to map
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
   * @param m0    the mapping function for extracting the first element
   * @param m1    the mapping function for extracting the second element
   * @param m2    the mapping function for extracting the third element
   * @param m3    the mapping function for extracting the fourth element
   * @param m4    the mapping function for extracting the fifth element
   * @param m5    the mapping function for extracting the sixth element
   * @param m6    the mapping function for extracting the seventh element
   * @param m7    the mapping function for extracting the eighth element
   * @param m8    the mapping function for extracting the ninth element
   * @param m9    the mapping function for extracting the tenth element
   * @param m10   the mapping function for extracting the eleventh element
   * @param m11   the mapping function for extracting the twelfth element
   * @param m12   the mapping function for extracting the thirteenth element
   * @param m13   the mapping function for extracting the fourteenth element
   * @param m14   the mapping function for extracting the fifteenth element
   * @param m15   the mapping function for extracting the sixteenth element
   * @param m16   the mapping function for extracting the seventeenth element
   * @return a function that produces septendecuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Function<T, Septendecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16) {
    return new SeptendecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16);
  }

  /**
   * Creates a function that maps objects to octodecuples.
   * 
   * @param <T>   the type of objects to map
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
   * @param m0    the mapping function for extracting the first element
   * @param m1    the mapping function for extracting the second element
   * @param m2    the mapping function for extracting the third element
   * @param m3    the mapping function for extracting the fourth element
   * @param m4    the mapping function for extracting the fifth element
   * @param m5    the mapping function for extracting the sixth element
   * @param m6    the mapping function for extracting the seventh element
   * @param m7    the mapping function for extracting the eighth element
   * @param m8    the mapping function for extracting the ninth element
   * @param m9    the mapping function for extracting the tenth element
   * @param m10   the mapping function for extracting the eleventh element
   * @param m11   the mapping function for extracting the twelfth element
   * @param m12   the mapping function for extracting the thirteenth element
   * @param m13   the mapping function for extracting the fourteenth element
   * @param m14   the mapping function for extracting the fifteenth element
   * @param m15   the mapping function for extracting the sixteenth element
   * @param m16   the mapping function for extracting the seventeenth element
   * @param m17   the mapping function for extracting the eighteenth element
   * @return a function that produces octodecuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> Function<T, Octodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17) {
    return new OctodecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17);
  }

  /**
   * Creates a function that maps objects to novemdecuples.
   * 
   * @param <T>   the type of objects to map
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
   * @param m0    the mapping function for extracting the first element
   * @param m1    the mapping function for extracting the second element
   * @param m2    the mapping function for extracting the third element
   * @param m3    the mapping function for extracting the fourth element
   * @param m4    the mapping function for extracting the fifth element
   * @param m5    the mapping function for extracting the sixth element
   * @param m6    the mapping function for extracting the seventh element
   * @param m7    the mapping function for extracting the eighth element
   * @param m8    the mapping function for extracting the ninth element
   * @param m9    the mapping function for extracting the tenth element
   * @param m10   the mapping function for extracting the eleventh element
   * @param m11   the mapping function for extracting the twelfth element
   * @param m12   the mapping function for extracting the thirteenth element
   * @param m13   the mapping function for extracting the fourteenth element
   * @param m14   the mapping function for extracting the fifteenth element
   * @param m15   the mapping function for extracting the sixteenth element
   * @param m16   the mapping function for extracting the seventeenth element
   * @param m17   the mapping function for extracting the eighteenth element
   * @param m18   the mapping function for extracting the nineteenth element
   * @return a function that produces novemdecuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> Function<T, Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17, Function<T, T18> m18) {
    return new NovemdecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17,
        m18);
  }

  /**
   * Creates a function that maps objects to vigintuples.
   * 
   * @param <T>   the type of objects to map
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
   * @param m0    the mapping function for extracting the first element
   * @param m1    the mapping function for extracting the second element
   * @param m2    the mapping function for extracting the third element
   * @param m3    the mapping function for extracting the fourth element
   * @param m4    the mapping function for extracting the fifth element
   * @param m5    the mapping function for extracting the sixth element
   * @param m6    the mapping function for extracting the seventh element
   * @param m7    the mapping function for extracting the eighth element
   * @param m8    the mapping function for extracting the ninth element
   * @param m9    the mapping function for extracting the tenth element
   * @param m10   the mapping function for extracting the eleventh element
   * @param m11   the mapping function for extracting the twelfth element
   * @param m12   the mapping function for extracting the thirteenth element
   * @param m13   the mapping function for extracting the fourteenth element
   * @param m14   the mapping function for extracting the fifteenth element
   * @param m15   the mapping function for extracting the sixteenth element
   * @param m16   the mapping function for extracting the seventeenth element
   * @param m17   the mapping function for extracting the eighteenth element
   * @param m18   the mapping function for extracting the nineteenth element
   * @param m19   the mapping function for extracting the twentieth element
   * @return a function that produces vigintuples
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> Function<T, Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>> toTuple(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17, Function<T, T18> m18, Function<T, T19> m19) {
    return new VigintupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17,
        m18, m19);
  }

}
