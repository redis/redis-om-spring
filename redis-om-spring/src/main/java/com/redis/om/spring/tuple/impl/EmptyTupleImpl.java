package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.EmptyTuple;

/**
 * Implementation of EmptyTuple (0-element tuple).
 */
public final class EmptyTupleImpl extends AbstractTuple implements EmptyTuple {

  /**
   * Singleton instance of the empty tuple.
   */
  public static final EmptyTuple EMPTY_TUPLE = new EmptyTupleImpl();

  /**
   * Private constructor for the singleton empty tuple.
   */
  private EmptyTupleImpl() {
    super(EmptyTupleImpl.class, new String[] {});
  }
}