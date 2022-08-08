package com.redis.om.spring.tuple;

public interface EmptyTuple extends Tuple {

  @Override
  default int size() {
    return 0;
  }
}