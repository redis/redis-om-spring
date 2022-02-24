package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface NineteenthAccessor<T, T18> extends TupleAccessor<T, T18> {

  @Override
  default int index() {
    return 18;
  }
}