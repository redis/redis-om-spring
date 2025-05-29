package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 2nd element of a tuple.
 * 
 * @param <T>  the tuple type
 * @param <T1> the type of the 2nd element
 */
@FunctionalInterface
public interface SecondAccessor<T, T1> extends TupleAccessor<T, T1> {

  @Override
  default int index() {
    return 1;
  }
}