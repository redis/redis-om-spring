package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 14th element of a tuple.
 * 
 * @param <T>   the tuple type
 * @param <T13> the type of the 14th element
 */
@FunctionalInterface
public interface FourteenthAccessor<T, T13> extends TupleAccessor<T, T13> {

  @Override
  default int index() {
    return 13;
  }
}