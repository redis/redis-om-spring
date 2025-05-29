package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 3rd element of a tuple.
 * 
 * @param <T>  the tuple type
 * @param <T2> the type of the 3rd element
 */
@FunctionalInterface
public interface ThirdAccessor<T, T2> extends TupleAccessor<T, T2> {

  @Override
  default int index() {
    return 2;
  }
}