package com.redis.om.spring.tuple;

public interface EmptyTuple extends Tuple {

  @Override
  default int size() {
    return 0;
  }

  default Object get(int index) {
    throw new IndexOutOfBoundsException(
        String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
  }
}