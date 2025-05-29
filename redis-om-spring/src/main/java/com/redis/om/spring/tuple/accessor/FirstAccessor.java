package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 1st element of a tuple.
 * 
 * @param <T>  the tuple type
 * @param <T0> the type of the 1st element
 */
@FunctionalInterface
public interface FirstAccessor<T, T0> extends TupleAccessor<T, T0> {

  @Override
  default int index() {
    return 0;
  }
}