package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.Tuple;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public abstract class AbstractTuple extends BasicAbstractTuple<AbstractTuple, Object> implements Tuple {

  protected AbstractTuple(Class<? extends AbstractTuple> baseClass, String[] labels, Object... values) {
    super(baseClass, labels, values);
  }

  @Override
  protected boolean isNullable() {
    return true;
  }

  @Override
  public Object get(int index) {
    return values[assertIndexBounds(index)];
  }

  protected int assertIndexBounds(int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException(
              "index " + index + " is illegal. The degree of this Tuple is " + size() + ".");
    }
    return index;
  }

  @Override
  public Stream<Object> stream() {
    return Stream.of(values);
  }
}
