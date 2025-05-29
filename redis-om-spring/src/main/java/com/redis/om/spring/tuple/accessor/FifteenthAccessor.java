package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 15th element of a tuple.
 * 
 * @param <T>   the tuple type
 * @param <T14> the type of the 15th element
 */
@FunctionalInterface
public interface FifteenthAccessor<T, T14> extends TupleAccessor<T, T14> {

  @Override
  default int index() {
    return 14;
  }
}