package com.redis.om.spring.tuple.impl;

import com.redis.om.spring.tuple.GenericTuple;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public abstract class BasicAbstractTuple<T extends GenericTuple<R>, R> implements GenericTuple<R> {

  protected final Object[] values;
  protected final String[] labels;
  protected final Class<? extends T> baseClass;

  BasicAbstractTuple(Class<? extends T> baseClass, String[] labels, Object... values) {
    requireNonNull(values);
    this.baseClass = requireNonNull(baseClass);
    this.labels = labels;

    if (!isNullable()) {
      for (Object v : values) {
        requireNonNull(v, () -> getClass().getName() + " cannot hold null values.");
      }
    }

    this.values = values;
  }

  protected abstract boolean isNullable();

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
    } else {
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

  @Override
  public Map<String, Object> labelledMap() {
    Map<String, Object> result = new HashMap<>();
    for (int i = 0; i < Math.min(labels.length, values.length); i++) {
      String label = StringUtils.removeStart(labels[i], "$.");
      Object value = values[i];
      result.put(label, value);
    }
    return Collections.unmodifiableMap(result);
  }

}
