package com.redis.om.spring.tuple.accessor;

import java.util.function.Function;

/**
 * Base interface for accessing tuple elements.
 * 
 * @param <T> the tuple type
 * @param <R> the return type of the accessed element
 */
public interface TupleAccessor<T, R> extends Function<T, R> {
  /**
   * Returns the index of the tuple element this accessor targets.
   *
   * @return the zero-based index of the tuple element
   */
  int index();
}