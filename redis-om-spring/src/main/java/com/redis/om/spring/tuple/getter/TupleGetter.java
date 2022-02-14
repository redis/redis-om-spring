
package com.redis.om.spring.tuple.getter;

import java.util.function.Function;

import com.redis.om.spring.tuple.Tuple;

/**
 * Function that given a {@link Tuple} returns the element at the
 * {@link #index() ordinal} position.
 *
 * @param <T> Tuple type
 * @param <R> return type
 */
public interface TupleGetter<T, R> extends Function<T, R> {

    /**
     * Returns the index of the tuple element that this getter returns.
     *
     * @return  the index of the tuple element that this getter returns
     */
    int index();
}