package com.redis.om.spring.tuple;

/**
 * A tuple with no elements.
 */
public interface EmptyTuple extends Tuple {

  @Override
  default int size() {
    return 0;
  }
}