package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface NinthAccessor<T, T8> extends TupleAccessor<T, T8> {

  @Override
  default int index() {
    return 8;
  }
}