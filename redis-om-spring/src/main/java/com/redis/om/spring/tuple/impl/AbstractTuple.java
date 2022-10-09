package com.redis.om.spring.tuple.impl;

import java.util.stream.Stream;

import com.redis.om.spring.tuple.Tuple;

public abstract class AbstractTuple extends BasicAbstractTuple<AbstractTuple, Object> implements Tuple {

  protected AbstractTuple(Class<? extends AbstractTuple> baseClass, String[] labels, Object... values) {
    super(baseClass, labels, values);
  }

  @Override
  protected boolean isNullable() {
    return false;
  }

  @Override
  public Object get(int index) {
    return values[assertIndexBounds(index)];
  }

  @Override
  public Stream<Object> stream() {
    return Stream.of(values);
  }
}
