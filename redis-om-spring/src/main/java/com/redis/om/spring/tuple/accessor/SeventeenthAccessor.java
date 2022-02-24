package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface SeventeenthAccessor<T, T16> extends TupleAccessor<T, T16> {

  @Override
  default int index() {
    return 16;
  }
}