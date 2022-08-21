package com.redis.om.spring.tuple;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class BasicAbstractTuple<T extends GenericTuple<R>, R> implements GenericTuple<R> {

  protected final Object[] values;
  protected final Class<? extends T> baseClass;

  BasicAbstractTuple(Class<? extends T> baseClass, Object... values) {
    requireNonNull(values);
    this.baseClass = requireNonNull(baseClass);
    if (!isNullable()) {
      for (Object v : values) {
        requireNonNull(v, () -> getClass().getName() + " cannot hold null values.");
      }
    }

    this.values = values;
  }

  protected abstract boolean isNullable();

  protected int assertIndexBounds(int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException(
          "index " + index + " is illegal. The degree of this Tuple is " + size() + ".");
    }
    return index;
  }

  @Override
  public int hashCode() {
    return Objects.hash(values);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!baseClass.isInstance(obj)) {
      return false;
    } else {//if (obj instanceof BasicAbstractTuple) {
      final BasicAbstractTuple<?, ?> tuple = (BasicAbstractTuple<?, ?>) obj;
      // Faster
      return Arrays.equals(this.values, tuple.values);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + Stream.of(values).map(Objects::toString).collect(joining(", ", "(", ")"));
  }

  @Override
  public <C> Stream<C> streamOf(Class<C> clazz) {
    requireNonNull(clazz);
    return Stream.of(values).filter(clazz::isInstance).map(clazz::cast);
  }

}
