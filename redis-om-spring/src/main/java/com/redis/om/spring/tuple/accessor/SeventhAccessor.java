package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface SeventhAccessor<T, T6> extends TupleAccessor<T, T6> {

  @Override
  default int index() {
    return 6;
  }
}