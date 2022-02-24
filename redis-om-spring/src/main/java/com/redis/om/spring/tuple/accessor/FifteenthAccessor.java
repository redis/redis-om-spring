package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface FifteenthAccessor<T, T14> extends TupleAccessor<T, T14> {

  @Override
  default int index() {
    return 14;
  }
}