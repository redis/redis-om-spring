package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface FirstAccessor<T, T0> extends TupleAccessor<T, T0> {

  @Override
  default int index() {
    return 0;
  }
}