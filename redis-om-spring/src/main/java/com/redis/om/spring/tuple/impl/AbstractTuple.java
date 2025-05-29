package com.redis.om.spring.tuple.impl;

import java.util.stream.Stream;

import com.redis.om.spring.tuple.Tuple;

/**
 * Abstract base implementation of {@link Tuple} that extends {@link BasicAbstractTuple}.
 * This class provides a concrete implementation for tuples that can contain null values
 * and implements the core tuple operations required by the Tuple interface.
 * 
 * <p>AbstractTuple serves as the foundation for all concrete tuple implementations in the
 * Redis OM Spring library. It handles index bounds checking, null value support, and
 * streaming operations while delegating the specific tuple construction logic to subclasses.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 * <li>Supports null values in tuple positions</li>
 * <li>Provides bounds checking for array access</li>
 * <li>Implements efficient streaming of tuple elements</li>
 * <li>Integrates with the BasicAbstractTuple infrastructure</li>
 * </ul>
 * 
 * @since 1.0
 * @see Tuple
 * @see BasicAbstractTuple
 */
public abstract class AbstractTuple extends BasicAbstractTuple<AbstractTuple, Object> implements Tuple {

  /**
   * Constructs a new AbstractTuple with the specified class type, labels, and values.
   * 
   * @param baseClass the class of the concrete tuple implementation, used for type checking
   * @param labels    the array of string labels for tuple elements, may be null
   * @param values    the values to store in this tuple, may contain null elements
   * @throws NullPointerException if baseClass is null
   */
  protected AbstractTuple(Class<? extends AbstractTuple> baseClass, String[] labels, Object... values) {
    super(baseClass, labels, values);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>AbstractTuple allows null values in any position.</p>
   * 
   * @return always true, indicating that null values are permitted
   */
  @Override
  protected boolean isNullable() {
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @param index the zero-based index of the element to retrieve
   * @return the element at the specified index, which may be null
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  @Override
  public Object get(int index) {
    return values[assertIndexBounds(index)];
  }

  /**
   * Validates that the given index is within the valid range for this tuple.
   * This method provides bounds checking for array access operations.
   * 
   * @param index the index to validate
   * @return the same index if it's valid
   * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size())
   */
  protected int assertIndexBounds(int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException(
          "index " + index + " is illegal. The degree of this Tuple is " + size() + ".");
    }
    return index;
  }

  /**
   * {@inheritDoc}
   * 
   * <p>This implementation creates a stream directly from the internal values array,
   * providing efficient access to all tuple elements.</p>
   * 
   * @return a sequential stream of all elements in this tuple
   */
  @Override
  public Stream<Object> stream() {
    return Stream.of(values);
  }
}
