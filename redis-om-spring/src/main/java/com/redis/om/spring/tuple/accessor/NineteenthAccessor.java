package com.redis.om.spring.tuple.accessor;

/**
 * Accessor for the 19th element of a tuple.
 * 
 * @param <T>   the tuple type
 * @param <T18> the type of the 19th element
 */
@FunctionalInterface
public interface NineteenthAccessor<T, T18> extends TupleAccessor<T, T18> {

  @Override
  default int index() {
    return 18;
  }
}