package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 17th element of a tuple.
 * 
 * @param <T>   the tuple type
 * @param <T16> the type of the 17th element
 */
@FunctionalInterface
public interface SeventeenthAccessor<T, T16> extends TupleAccessor<T, T16> {

  @Override
  default int index() {
    return 16;
  }
}