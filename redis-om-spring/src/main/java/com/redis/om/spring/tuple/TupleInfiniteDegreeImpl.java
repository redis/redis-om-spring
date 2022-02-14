
package com.redis.om.spring.tuple;

/**
 * An implementation class of a {@link Tuple } with infinite degree. Sadly,
 * types are lost for this implementation.
 */
public final class TupleInfiniteDegreeImpl extends AbstractTuple implements Tuple {

  public TupleInfiniteDegreeImpl(Object... elements) {
    super(TupleInfiniteDegreeImpl.class, elements);
  }

  @Override
  public int size() {
    return values.length;
  }

}
