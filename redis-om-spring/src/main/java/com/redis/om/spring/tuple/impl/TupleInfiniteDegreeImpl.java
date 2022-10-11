package com.redis.om.spring.tuple.impl;

public final class TupleInfiniteDegreeImpl extends AbstractTuple {

  public TupleInfiniteDegreeImpl(Object... elements) {
    super(TupleInfiniteDegreeImpl.class, new String[] {}, elements);
  }

  @Override
  public int size() {
    return values.length;
  }

}
