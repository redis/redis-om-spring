
package com.redis.om.spring.tuple;

/**
 * This interface defines a generic {@link Tuple} of degree 0 that can hold
 * non-null values. A Tuple is type safe, immutable and thread safe. For tuples
 * that can hold null elements see {@link OptionalTuple}. For mutable tuples see
 * {@link MutableTuple}
 *
 * This {@link Tuple} has a degree of 0
 * <p>
 * 
 * @see Tuple
 * @see OptionalTuple
 * @see MutableTuple
 */
public interface EmptyTuple extends Tuple {

  @Override
  default int size() {
    return 0;
  }

  default Object get(int index) {
    throw new IndexOutOfBoundsException(
        String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
  }
}