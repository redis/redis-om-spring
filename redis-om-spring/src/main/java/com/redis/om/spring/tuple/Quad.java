
package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.getter.TupleGetter;
import com.redis.om.spring.tuple.getter.TupleGetter0;
import com.redis.om.spring.tuple.getter.TupleGetter1;
import com.redis.om.spring.tuple.getter.TupleGetter2;
import com.redis.om.spring.tuple.getter.TupleGetter3;

/**
 * This interface defines a generic {@link Tuple} of degree 4 that can hold
 * non-null values. A Tuple is type safe, immutable and thread safe. For tuples
 * that can hold null elements see {@link OptionalTuple}. For mutable tuples see
 * {@link MutableTuple}
 *
 * This {@link Tuple} has a degree of 4
 * <p>
 *
 *
 * @param <T0> type of element 0
 * @param <T1> type of element 1
 * @param <T2> type of element 2
 * @param <T3> type of element 3
 *
 * @see Tuple
 * @see OptionalTuple
 * @see MutableTuple
 */
public interface Quad<T0, T1, T2, T3> extends Tuple {

  /**
   * Returns the 0th element from this tuple.
   *
   * @return the 0th element from this tuple.
   */
  T0 get0();

  /**
   * Returns the 1st element from this tuple.
   *
   * @return the 1st element from this tuple.
   */
  T1 get1();

  /**
   * Returns the 2nd element from this tuple.
   *
   * @return the 2nd element from this tuple.
   */
  T2 get2();

  /**
   * Returns the 3rd element from this tuple.
   *
   * @return the 3rd element from this tuple.
   */
  T3 get3();

  @Override
  default int size() {
    return 4;
  }

  default Object get(int index) {
    switch (index) {
      case 0:
        return get0();
      case 1:
        return get1();
      case 2:
        return get2();
      case 3:
        return get3();
      default:
        throw new IndexOutOfBoundsException(
            String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    }
  }

  /**
   * Returns a {@link TupleGetter getter} for the 0th element in the {@code
   * Tuple}.
   *
   * @return the element at the 0th position
   * @param <T0> the 0th element type
   * @param <T1> the 1st element type
   * @param <T2> the 2nd element type
   * @param <T3> the 3rd element type
   */
  static <T0, T1, T2, T3> TupleGetter0<Quad<T0, T1, T2, T3>, T0> getter0() {
    return Quad::get0;
  }

  /**
   * Returns a {@link TupleGetter getter} for the 1st element in the {@code
   * Tuple}.
   *
   * @return the element at the 1st position
   * @param <T0> the 0th element type
   * @param <T1> the 1st element type
   * @param <T2> the 2nd element type
   * @param <T3> the 3rd element type
   */
  static <T0, T1, T2, T3> TupleGetter1<Quad<T0, T1, T2, T3>, T1> getter1() {
    return Quad::get1;
  }

  /**
   * Returns a {@link TupleGetter getter} for the 2nd element in the {@code
   * Tuple}.
   *
   * @return the element at the 2nd position
   * @param <T0> the 0th element type
   * @param <T1> the 1st element type
   * @param <T2> the 2nd element type
   * @param <T3> the 3rd element type
   */
  static <T0, T1, T2, T3> TupleGetter2<Quad<T0, T1, T2, T3>, T2> getter2() {
    return Quad::get2;
  }

  /**
   * Returns a {@link TupleGetter getter} for the 3rd element in the {@code
   * Tuple}.
   *
   * @return the element at the 3rd position
   * @param <T0> the 0th element type
   * @param <T1> the 1st element type
   * @param <T2> the 2nd element type
   * @param <T3> the 3rd element type
   */
  static <T0, T1, T2, T3> TupleGetter3<Quad<T0, T1, T2, T3>, T3> getter3() {
    return Quad::get3;
  }
}