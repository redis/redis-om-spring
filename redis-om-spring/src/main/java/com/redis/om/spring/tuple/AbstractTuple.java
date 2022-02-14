
package com.redis.om.spring.tuple;

import java.util.stream.Stream;

/**
 *
 */
public abstract class AbstractTuple extends BasicAbstractTuple<AbstractTuple, Object> implements Tuple {

  protected AbstractTuple(Class<? extends AbstractTuple> baseClass, Object... values) {
    super(baseClass, values);
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
