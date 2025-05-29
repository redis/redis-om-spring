package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 16th element of a tuple.
 * 
 * @param <T>   the tuple type
 * @param <T15> the type of the 16th element
 */
@FunctionalInterface
public interface SixteenthAccessor<T, T15> extends TupleAccessor<T, T15> {

  @Override
  default int index() {
    return 15;
  }
}