package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface TwentiethAccessor<T, T19> extends TupleAccessor<T, T19> {

  @Override
  default int index() {
    return 19;
  }
}