package com.redis.om.spring.tuple;

public final class TupleInfiniteDegreeOfNullablesImpl extends AbstractTupleOfNullables implements OptionalTuple {

  public TupleInfiniteDegreeOfNullablesImpl(Object... elements) {
    super(TupleInfiniteDegreeOfNullablesImpl.class, elements);
  }

  @Override
  public int size() {
    return values.length;
  }

}
