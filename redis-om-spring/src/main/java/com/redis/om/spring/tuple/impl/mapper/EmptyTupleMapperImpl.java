package com.redis.om.spring.tuple.impl.mapper;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.EmptyTuple;
import com.redis.om.spring.tuple.Tuples;

public final class EmptyTupleMapperImpl<T> extends AbstractTupleMapper<T, EmptyTuple> {

  public static final EmptyTupleMapperImpl<?> EMPTY_MAPPER = new EmptyTupleMapperImpl<>();

  private EmptyTupleMapperImpl() {
    super(0);
  }

  @Override
  public EmptyTuple apply(T t) {
    return Tuples.of(

    );
  }
}