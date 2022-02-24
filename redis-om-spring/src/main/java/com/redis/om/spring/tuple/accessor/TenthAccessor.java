package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface TenthAccessor<T, T9> extends TupleAccessor<T, T9> {

  @Override
  default int index() {
    return 9;
  }
}