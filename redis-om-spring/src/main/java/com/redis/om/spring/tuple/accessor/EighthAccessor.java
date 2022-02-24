package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface EighthAccessor<T, T7> extends TupleAccessor<T, T7> {

  @Override
  default int index() {
    return 7;
  }
}