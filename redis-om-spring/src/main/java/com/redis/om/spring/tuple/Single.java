
package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.getter.TupleGetter;
import com.redis.om.spring.tuple.getter.TupleGetter0;

/**
 * This interface defines a generic {@link Tuple} of degree 1 that can hold
 * non-null values. A Tuple is type safe, immutable and thread safe. For tuples
 * that can hold null elements see {@link OptionalTuple}. For mutable tuples see
 * {@link MutableTuple}
 *
 * This {@link Tuple} has a degree of 1
 * <p>
 *
 *
 * @param <T0> type of element 0
 *
 * @see Tuple
 * @see OptionalTuple
 * @see MutableTuple
 */
public interface Single<T0> extends Tuple {

  /**
   * Returns the 0th element from this tuple.
   *
   * @return the 0th element from this tuple.
   */
  T0 get0();

  @Override
  default int size() {
    return 1;
  }

  default Object get(int index) {
    if (index == 0) {
      return get0();
    } else {
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
   */
  static <T0> TupleGetter0<Single<T0>, T0> getter0() {
    return Single::get0;
  }
}