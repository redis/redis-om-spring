package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface SecondAccessor<T, T1> extends TupleAccessor<T, T1> {

  @Override
  default int index() {
    return 1;
  }
}