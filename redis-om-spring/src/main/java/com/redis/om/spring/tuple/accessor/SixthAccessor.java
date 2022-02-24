package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface SixthAccessor<T, T5> extends TupleAccessor<T, T5> {

  @Override
  default int index() {
    return 5;
  }
}