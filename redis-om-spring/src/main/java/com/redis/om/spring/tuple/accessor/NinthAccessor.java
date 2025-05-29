package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 9th element of a tuple.
 * 
 * @param <T>  the tuple type
 * @param <T8> the type of the 9th element
 */
@FunctionalInterface
public interface NinthAccessor<T, T8> extends TupleAccessor<T, T8> {

  @Override
  default int index() {
    return 8;
  }
}