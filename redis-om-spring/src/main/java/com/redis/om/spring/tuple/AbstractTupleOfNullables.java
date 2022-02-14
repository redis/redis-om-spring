
package com.redis.om.spring.tuple;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 *
 */
public abstract class AbstractTupleOfNullables extends BasicAbstractTuple<AbstractTupleOfNullables, Optional<Object>>
    implements OptionalTuple {

  protected AbstractTupleOfNullables(Class<? extends AbstractTupleOfNullables> baseClass, Object... values) {
    super(baseClass, values);
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