package com.redis.om.spring.tuple.impl;

/**
 * Implementation of a tuple with variable degree (unbounded number of elements).
 * This class is used when the number of elements exceeds the maximum degree
 * of the fixed-size tuple implementations (20 elements).
 * 
 * <p>Unlike fixed-degree tuples, this implementation can hold any number of
 * elements, making it suitable for dynamic tuple creation where the element
 * count is not known at compile time or exceeds 20.</p>
 * 
 * @see AbstractTuple
 */
public final class TupleInfiniteDegreeImpl extends AbstractTuple {

  /**
   * Creates a new variable-degree tuple with the specified elements.
   * 
   * @param elements the elements to store in the tuple, can be any number of elements
   */
  public TupleInfiniteDegreeImpl(Object... elements) {
    super(TupleInfiniteDegreeImpl.class, new String[] {}, elements);
  }

  @Override
  public int size() {
    return values.length;
  }

}
