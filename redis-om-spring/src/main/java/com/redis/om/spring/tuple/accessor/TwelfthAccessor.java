package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 12th element of a tuple.
 * 
 * @param <T>   the tuple type
 * @param <T11> the type of the 12th element
 */
@FunctionalInterface
public interface TwelfthAccessor<T, T11> extends TupleAccessor<T, T11> {

  @Override
  default int index() {
    return 11;
  }
}