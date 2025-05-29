package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 5th element of a tuple.
 * 
 * @param <T>  the tuple type
 * @param <T4> the type of the 5th element
 */
@FunctionalInterface
public interface FifthAccessor<T, T4> extends TupleAccessor<T, T4> {

  @Override
  default int index() {
    return 4;
  }
}