package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface ThirteenthAccessor<T, T12> extends TupleAccessor<T, T12> {

  @Override
  default int index() {
    return 12;
  }
}