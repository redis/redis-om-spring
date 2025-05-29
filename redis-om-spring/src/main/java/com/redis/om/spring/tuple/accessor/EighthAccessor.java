package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 8th element of a tuple.
 * 
 * @param <T>  the tuple type
 * @param <T7> the type of the 8th element
 */
@FunctionalInterface
public interface EighthAccessor<T, T7> extends TupleAccessor<T, T7> {

  @Override
  default int index() {
    return 7;
  }
}