package com.redis.om.spring.tuple;

public final class TupleInfiniteDegreeImpl extends AbstractTuple implements Tuple {

  public TupleInfiniteDegreeImpl(Object... elements) {
    super(TupleInfiniteDegreeImpl.class, elements);
  }

  @Override
  public int size() {
    return values.length;
  }

}
