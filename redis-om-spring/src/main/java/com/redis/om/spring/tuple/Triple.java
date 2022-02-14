
package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.getter.TupleGetter;
import com.redis.om.spring.tuple.getter.TupleGetter0;
import com.redis.om.spring.tuple.getter.TupleGetter1;
import com.redis.om.spring.tuple.getter.TupleGetter2;

/**
 * This interface defines a generic {@link Tuple} of degree 3 that can hold
 * non-null values. A Tuple is type safe, immutable and thread safe. For tuples
 * that can hold null elements see {@link OptionalTuple}. For mutable tuples see
 * {@link MutableTuple}
 *
 * This {@link Tuple} has a degree of 3
 * <p>
 *
 *
 * @param <T0> type of element 0
 * @param <T1> type of element 1
 * @param <T2> type of element 2
 *
 * @see Tuple
 * @see OptionalTuple
 * @see MutableTuple
 */
public interface Triple<T0, T1, T2> extends Tuple {

  /**
   * Returns the 0th element from this tuple.
   *
   * @return the 0th element from this tuple.
   */
  T0 getFirst();

  /**
   * Returns the 1st element from this tuple.
   *
   * @return the 1st element from this tuple.
   */
  T1 getSecond();

  /**
   * Returns the 2nd element from this tuple.
   *
   * @return the 2nd element from this tuple.
   */
  T2 getThird();

  @Override
  default int size() {
    return 3;
  }

  default Object get(int index) {
    switch (index) {
      case 0:
        return getFirst();
      case 1:
        return getSecond();
      case 2:
        return getThird();
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
   */
  static <T0, T1, T2> TupleGetter0<Triple<T0, T1, T2>, T0> getter0() {
    return Triple::getFirst;
  }

  /**
   * Returns a {@link TupleGetter getter} for the 1st element in the {@code
   * Tuple}.
   *
   * @return the element at the 1st position
   * @param <T0> the 0th element type
   * @param <T1> the 1st element type
   * @param <T2> the 2nd element type
   */
  static <T0, T1, T2> TupleGetter1<Triple<T0, T1, T2>, T1> getter1() {
    return Triple::getSecond;
  }

  /**
   * Returns a {@link TupleGetter getter} for the 2nd element in the {@code
   * Tuple}.
   *
   * @return the element at the 2nd position
   * @param <T0> the 0th element type
   * @param <T1> the 1st element type
   * @param <T2> the 2nd element type
   */
  static <T0, T1, T2> TupleGetter2<Triple<T0, T1, T2>, T2> getter2() {
    return Triple::getThird;
  }
}