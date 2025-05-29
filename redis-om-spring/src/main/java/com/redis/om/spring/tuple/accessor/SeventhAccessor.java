package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 7th element of a tuple.
 * 
 * @param <T>  the tuple type
 * @param <T6> the type of the 7th element
 */
@FunctionalInterface
public interface SeventhAccessor<T, T6> extends TupleAccessor<T, T6> {

  @Override
  default int index() {
    return 6;
  }
}