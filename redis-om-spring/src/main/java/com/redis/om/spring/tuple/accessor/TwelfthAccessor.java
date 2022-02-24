package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface TwelfthAccessor<T, T11> extends TupleAccessor<T, T11> {

  @Override
  default int index() {
    return 11;
  }
}