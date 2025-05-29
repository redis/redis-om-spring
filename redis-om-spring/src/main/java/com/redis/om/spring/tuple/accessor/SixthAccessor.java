package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 6th element of a tuple.
 * 
 * @param <T>  the tuple type
 * @param <T5> the type of the 6th element
 */
@FunctionalInterface
public interface SixthAccessor<T, T5> extends TupleAccessor<T, T5> {

  @Override
  default int index() {
    return 5;
  }
}