package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface EighteenthAccessor<T, T17> extends TupleAccessor<T, T17> {

  @Override
  default int index() {
    return 17;
  }
}