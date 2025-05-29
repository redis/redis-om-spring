package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 11th element of a tuple.
 * 
 * @param <T>   the tuple type
 * @param <T10> the type of the 11th element
 */
@FunctionalInterface
public interface EleventhAccessor<T, T10> extends TupleAccessor<T, T10> {

  @Override
  default int index() {
    return 10;
  }
}