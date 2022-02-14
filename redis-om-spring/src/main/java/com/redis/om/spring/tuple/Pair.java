
package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.getter.TupleGetter;
import com.redis.om.spring.tuple.getter.TupleGetter0;
import com.redis.om.spring.tuple.getter.TupleGetter1;

/**
 * This interface defines a generic {@link Tuple} of degree 2 that can hold
 * non-null values. A Tuple is type safe, immutable and thread safe. For tuples
 * that can hold null elements see {@link OptionalTuple}. For mutable tuples see
 * {@link MutableTuple}
 *
 * This {@link Tuple} has a degree of 2
 * <p>
 *
 *
 * @param <T0> type of element 0
 * @param <T1> type of element 1
 *
 * @see Tuple
 * @see OptionalTuple
 * @see MutableTuple
 */
public interface Pair<T0, T1> extends Tuple {

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

  @Override
  default int size() {
    return 2;
  }

  default Object get(int index) {
    switch (index) {
      case 0:
        return getFirst();
      case 1:
        return getSecond();
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
   */
  static <T0, T1> TupleGetter0<Pair<T0, T1>, T0> getter0() {
    return Pair::getFirst;
  }

  /**
   * Returns a {@link TupleGetter getter} for the 1st element in the {@code
   * Tuple}.
   *
   * @return the element at the 1st position
   * @param <T0> the 0th element type
   * @param <T1> the 1st element type
   */
  static <T0, T1> TupleGetter1<Pair<T0, T1>, T1> getter1() {
    return Pair::getSecond;
  }
}