package com.redis.om.spring.tuple.accessor;

@FunctionalInterface
public interface FourthAccessor<T, T3> extends TupleAccessor<T, T3> {

    @Override
    default int index() {
        return 3;
    }
}