package com.redis.om.spring.tuple.impl.mapper;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.EmptyTuple;
import com.redis.om.spring.tuple.Tuples;

/**
 * Implementation of EmptyTupleMapper for mapping to empty tuples.
 * 
 * @param <T> the type of the source object being mapped from
 */
public final class EmptyTupleMapperImpl<T> extends AbstractTupleMapper<T, EmptyTuple> {

  /**
   * Singleton instance of the empty tuple mapper.
   */
  public static final EmptyTupleMapperImpl<?> EMPTY_MAPPER = new EmptyTupleMapperImpl<>();

  /**
   * Private constructor for the singleton empty tuple mapper.
   */
  private EmptyTupleMapperImpl() {
    super(0);
  }

  @Override
  public EmptyTuple apply(T t) {
    return Tuples.of(

    );
  }
}