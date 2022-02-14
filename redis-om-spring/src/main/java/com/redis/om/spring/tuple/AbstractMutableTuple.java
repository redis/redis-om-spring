
package com.redis.om.spring.tuple;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 *
 */
public abstract class AbstractMutableTuple extends BasicAbstractTuple<AbstractMutableTuple, Optional<Object>>
    implements MutableTuple {

  protected AbstractMutableTuple(Class<? extends AbstractMutableTuple> baseClass, int degree) {
    super(baseClass, new Object[degree]);
  }

  @Override
  protected boolean isNullable() {
    return true;
  }

  @Override
  public Optional<Object> get(int index) {
    return Optional.ofNullable(values[assertIndexBounds(index)]);
  }

  @Override
  public Stream<Optional<Object>> stream() {
    return Stream.of(values).map(Optional::ofNullable);
  }

  @Override
  public <C> Stream<C> streamOf(Class<C> clazz) {
    requireNonNull(clazz);
    return Stream.of(values).filter(clazz::isInstance).map(clazz::cast);
  }

}