
package com.redis.om.spring.tuple;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * The BasicAbstractTuple implements parts of a generic Tuple of any order.
 * Tuple elements are stored in an array.
 *
 * @param <T> type of BasicTuple
 * @param <R> The return type of {@link #get(int)}
 */
public abstract class BasicAbstractTuple<T extends BasicTuple<R>, R> implements BasicTuple<R> {

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

    // Defensive copying
    this.values = Arrays.copyOf(values, values.length);

    if (values.length != size()) {
      throw new IllegalArgumentException("A Tuple of degree " + size() + " must contain exactly " + size()
          + " elements. Element length was " + values.length);
    }
  }

  /**
   * Returns if this Tuple can contain null elements.
   *
   * @return if this Tuple can contain null elements
   */
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
    }
    if (obj instanceof BasicAbstractTuple) {
      final BasicAbstractTuple<?, ?> tuple = (BasicAbstractTuple<?, ?>) obj;
      // Faster
      return Arrays.equals(this.values, tuple.values);
    }
    // Must be a BasicTuple since baseClass is a BasicTuple
    final BasicTuple<?> tuple = (BasicTuple<?>) obj;
    final int capacity = tuple.size();
    for (int i = 0; i < capacity; i++) {
      if (!Objects.equals(get(i), tuple.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + Stream.of(values).map(Objects::toString).collect(joining(", ", "{", "}"));
  }

  @Override
  public <C> Stream<C> streamOf(Class<C> clazz) {
    requireNonNull(clazz);
    return Stream.of(values).filter(clazz::isInstance).map(clazz::cast);
  }

}
