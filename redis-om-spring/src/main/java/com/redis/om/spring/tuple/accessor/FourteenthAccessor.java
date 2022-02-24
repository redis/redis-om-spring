package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface FourteenthAccessor<T, T13> extends TupleAccessor<T, T13> {

  @Override
  default int index() {
    return 13;
  }
}