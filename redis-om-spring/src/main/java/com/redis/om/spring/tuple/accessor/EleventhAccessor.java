package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface EleventhAccessor<T, T10> extends TupleAccessor<T, T10> {

  @Override
  default int index() {
    return 10;
  }
}