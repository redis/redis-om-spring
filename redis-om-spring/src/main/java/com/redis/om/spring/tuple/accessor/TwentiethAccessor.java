package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 20th element of a tuple.
 * 
 * @param <T>   the tuple type
 * @param <T19> the type of the 20th element
 */
@FunctionalInterface
public interface TwentiethAccessor<T, T19> extends TupleAccessor<T, T19> {

  @Override
  default int index() {
    return 19;
  }
}