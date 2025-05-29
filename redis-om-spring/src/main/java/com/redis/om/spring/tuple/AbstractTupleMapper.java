package com.redis.om.spring.tuple;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

/**
 * Abstract base class that provides a skeletal implementation of {@link TupleMapper}.
 * This class handles the common functionality of storing and managing mapping functions
 * while leaving the specific tuple construction logic to concrete subclasses.
 * 
 * <p>AbstractTupleMapper maintains an array of mapping functions that correspond to
 * individual tuple elements. Each mapping function extracts a specific value from
 * the source object of type T.</p>
 * 
 * <p>Subclasses must implement the {@link Function#apply(Object)} method to define
 * how the individual mapping functions are combined to create the resulting tuple.</p>
 * 
 * <p>This class is thread-safe once all mapping functions have been set during
 * construction of the concrete subclass.</p>
 * 
 * @param <T> the type of the input object to be mapped
 * @param <R> the type of the resulting tuple
 * 
 * @since 1.0
 * @see TupleMapper
 */
public abstract class AbstractTupleMapper<T, R> implements TupleMapper<T, R> {

  private final Function<T, ?>[] mappers;

  /**
   * Constructs a new AbstractTupleMapper with the specified degree.
   * This constructor initializes the internal array to hold the mapping functions.
   * 
   * @param degree the number of elements in tuples produced by this mapper, must be non-negative
   * @throws IllegalArgumentException if degree is negative
   */
  @SuppressWarnings(
    { "unchecked" }
  )
  protected AbstractTupleMapper(int degree) {
    this.mappers = new Function[degree];
  }

  /**
   * {@inheritDoc}
   * 
   * @return the number of mapping functions (degree) configured for this mapper
   */
  @Override
  public final int degree() {
    return mappers.length;
  }

  /**
   * {@inheritDoc}
   * 
   * @param index the zero-based index of the mapping function to retrieve
   * @return the mapping function at the specified index
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  @Override
  public final Function<T, ?> get(int index) {
    return mappers[index];
  }

  /**
   * Retrieves and casts the mapping function at the specified index to a specific type.
   * This is a convenience method for subclasses that need type-safe access to their
   * mapping functions.
   * 
   * @param <C>   the expected return type of the mapping function
   * @param index the zero-based index of the mapping function to retrieve
   * @return the mapping function at the specified index, cast to the expected type
   * @throws IndexOutOfBoundsException if the index is out of range
   * @throws ClassCastException        if the mapping function cannot be cast to the expected type
   */
  @SuppressWarnings(
    "unchecked"
  )
  protected final <C> Function<T, C> getAndCast(int index) {
    return (Function<T, C>) mappers[index];
  }

  /**
   * Sets the mapping function at the specified index.
   * This method is used during construction to populate the mapper with the individual
   * field extraction functions.
   * 
   * @param index  the zero-based index where to store the mapping function
   * @param mapper the mapping function to store, must not be null
   * @throws IndexOutOfBoundsException if the index is out of range
   * @throws NullPointerException      if mapper is null
   */
  protected final void set(int index, Function<T, ?> mapper) {
    mappers[index] = requireNonNull(mapper);
  }

}
