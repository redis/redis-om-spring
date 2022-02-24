package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface FifthAccessor<T, T4> extends TupleAccessor<T, T4> {

    @Override
    default int index() {
        return 4;
    }
}