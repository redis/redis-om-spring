package com.redis.om.spring.tuple.impl;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.redis.om.spring.tuple.GenericTuple;

/**
 * Abstract base class that provides the fundamental implementation for all tuple types.
 * This class serves as the core infrastructure for tuple implementations, handling
 * value storage, equality, hashing, and labeling functionality.
 * 
 * <p>BasicAbstractTuple provides the foundational functionality needed by all tuple
 * implementations in the Redis OM Spring library. It manages the internal array of values,
 * provides consistent equals/hashCode implementation, and handles the creation of
 * labeled maps from tuple data.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 * <li>Value storage and management</li>
 * <li>Null value handling based on subclass policy</li>
 * <li>Equality and hash code computation</li>
 * <li>String representation generation</li>
 * <li>Type-safe streaming operations</li>
 * <li>Label-to-value mapping creation</li>
 * </ul>
 * 
 * <p>This class is designed to be extended by concrete tuple implementations that
 * specify the exact tuple type and element type parameters.</p>
 * 
 * @param <T> the concrete tuple type that extends GenericTuple&lt;R&gt;
 * @param <R> the type of elements contained in this tuple
 * 
 * @since 1.0
 * @see GenericTuple
 */
public abstract class BasicAbstractTuple<T extends GenericTuple<R>, R> implements GenericTuple<R> {

  /** The array containing the actual tuple values */
  protected final Object[] values;

  /** The array containing labels for tuple elements, may be null */
  protected final String[] labels;

  /** The base class used for type checking in equality operations */
  protected final Class<? extends T> baseClass;

  /**
   * Constructs a new BasicAbstractTuple with the specified parameters.
   * 
   * @param baseClass the class of the concrete tuple implementation, used for type checking in equals()
   * @param labels    the array of string labels for tuple elements, may be null
   * @param values    the values to store in this tuple, must not be null
   * @throws NullPointerException if baseClass or values is null
   * @throws NullPointerException if isNullable() returns false and any value is null
   */
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

  /**
   * Determines whether this tuple implementation allows null values.
   * Subclasses must implement this method to specify their null value policy.
   * 
   * @return true if null values are allowed, false otherwise
   */
  protected abstract boolean isNullable();

  /**
   * {@inheritDoc}
   * 
   * <p>The size is determined by the length of the internal values array.</p>
   * 
   * @return the number of elements in this tuple
   */
  @Override
  public int size() {
    return values.length;
  }

  /**
   * {@inheritDoc}
   * 
   * <p>Hash code is computed based on the contents of the values array.</p>
   * 
   * @return a hash code value for this tuple
   */
  @Override
  public int hashCode() {
    return Objects.hash(values);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>Two tuples are equal if they are of the same type (based on baseClass)
   * and contain the same values in the same order. This implementation uses
   * Arrays.equals() for efficient comparison.</p>
   * 
   * @param obj the object to compare with this tuple
   * @return true if the objects are equal, false otherwise
   */
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

  /**
   * {@inheritDoc}
   * 
   * <p>Returns a string representation in the format "ClassName (value1, value2, ...)".</p>
   * 
   * @return a string representation of this tuple
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + Stream.of(values).map(Objects::toString).collect(joining(", ", "(", ")"));
  }

  /**
   * {@inheritDoc}
   * 
   * @param clazz the class to filter by, must not be null
   * @return a stream of elements that are instances of the specified class
   * @throws NullPointerException if clazz is null
   */
  @Override
  public <C> Stream<C> streamOf(Class<C> clazz) {
    requireNonNull(clazz);
    return Stream.of(values).filter(clazz::isInstance).map(clazz::cast);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>Creates a map by pairing labels with their corresponding values. Labels that
   * start with "$." have this prefix removed. The returned map is immutable.</p>
   * 
   * @return an immutable map containing labeled tuple elements
   */
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
