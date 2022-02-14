
package com.redis.om.spring.tuple;

/**
 * An implementation class of a {@link OptionalTuple} Sadly, types are lost for
 * this implementation.
 */
public final class TupleInfiniteDegreeOfNullablesImpl extends AbstractTupleOfNullables implements OptionalTuple {

  public TupleInfiniteDegreeOfNullablesImpl(Object... elements) {
    super(TupleInfiniteDegreeOfNullablesImpl.class, elements);
  }

  @Override
  public int size() {
    return values.length;
  }

}
