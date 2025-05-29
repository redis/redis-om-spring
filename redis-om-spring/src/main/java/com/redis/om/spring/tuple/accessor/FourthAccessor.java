package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 4th element of a tuple.
 * 
 * @param <T>  the tuple type
 * @param <T3> the type of the 4th element
 */
@FunctionalInterface
public interface FourthAccessor<T, T3> extends TupleAccessor<T, T3> {

  @Override
  default int index() {
    return 3;
  }
}