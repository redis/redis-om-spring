package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface SixteenthAccessor<T, T15> extends TupleAccessor<T, T15> {

  @Override
  default int index() {
    return 15;
  }
}