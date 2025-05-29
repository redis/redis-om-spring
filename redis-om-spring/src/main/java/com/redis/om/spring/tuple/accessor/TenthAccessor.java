package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 10th element of a tuple.
 * 
 * @param <T>  the tuple type
 * @param <T9> the type of the 10th element
 */
@FunctionalInterface
public interface TenthAccessor<T, T9> extends TupleAccessor<T, T9> {

  @Override
  default int index() {
    return 9;
  }
}