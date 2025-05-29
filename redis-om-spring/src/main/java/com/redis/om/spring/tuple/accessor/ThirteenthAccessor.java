package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 13th element of a tuple.
 * 
 * @param <T>   the tuple type
 * @param <T12> the type of the 13th element
 */
@FunctionalInterface
public interface ThirteenthAccessor<T, T12> extends TupleAccessor<T, T12> {

  @Override
  default int index() {
    return 12;
  }
}