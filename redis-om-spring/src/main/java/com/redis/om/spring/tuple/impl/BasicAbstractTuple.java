package com.redis.om.spring.tuple.impl;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.redis.om.spring.tuple.GenericTuple;

import org.apache.commons.lang3.StringUtils;

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
  public Map<String,Object> labelledMap() {
    Stream<String> objectLabels = Arrays.stream(labels).map(s -> StringUtils.removeStart(s, "$."));
    return Streams.zip(objectLabels, Arrays.stream(values), Maps::immutableEntry)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

}
