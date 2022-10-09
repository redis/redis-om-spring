package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Single;
import com.redis.om.spring.tuple.Tuples;

public final class SingleMapperImpl<T, T0>
extends AbstractTupleMapper<T, Single<T0>> {

    public SingleMapperImpl(Function<T, T0> m0) {
        super(1);
        set(0, m0);
    }

    @Override
    public Single<T0> apply(T t) {
        return Tuples.of(
            getFirst().apply(t)
        );
    }

    public Function<T, T0> getFirst() {
        return getAndCast(0);
    }
}