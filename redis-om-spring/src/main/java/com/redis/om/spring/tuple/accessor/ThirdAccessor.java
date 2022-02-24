package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface ThirdAccessor<T, T2> extends TupleAccessor<T, T2> {

  @Override
  default int index() {
    return 2;
  }
}