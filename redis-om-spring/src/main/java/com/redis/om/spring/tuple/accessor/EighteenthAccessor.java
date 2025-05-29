package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 18th element of a tuple.
 * 
 * @param <T>   the tuple type
 * @param <T17> the type of the 18th element
 */
@FunctionalInterface
public interface EighteenthAccessor<T, T17> extends TupleAccessor<T, T17> {

  @Override
  default int index() {
    return 17;
  }
}