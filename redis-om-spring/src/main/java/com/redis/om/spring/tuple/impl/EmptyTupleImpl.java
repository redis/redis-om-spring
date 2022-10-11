package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.EmptyTuple;

public final class EmptyTupleImpl extends AbstractTuple implements EmptyTuple {

  public static final EmptyTuple EMPTY_TUPLE = new EmptyTupleImpl();

  private EmptyTupleImpl() {
    super(EmptyTupleImpl.class, new String[] {});
  }
}