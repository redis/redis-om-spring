package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Triple;
import com.redis.om.spring.tuple.Tuples;

/**
 * Mapper implementation for 3-element tuples.
 * <p>
 * This class provides functionality to map objects of type T to {@link Triple} tuples
 * by applying three separate mapping functions. Each mapping function extracts a
 * specific value from the source object to populate the corresponding position in
 * the resulting triple.
 * </p>
 * <p>
 * The mapper is particularly useful in stream operations and functional transformations
 * where you need to extract multiple values from a single object and package them
 * into a triple for further processing.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * TripleMapperImpl<Person, String, Integer, Boolean> mapper = new TripleMapperImpl<>(
 *     Person::getName,
 *     Person::getAge,
 *     Person::isActive
 * );
 * Triple<String, Integer, Boolean> result = mapper.apply(person);
 * }</pre>
 *
 * @param <T>  the type of the source object
 * @param <T0> the type of the first element in the resulting triple
 * @param <T1> the type of the second element in the resulting triple
 * @param <T2> the type of the third element in the resulting triple
 * @see AbstractTupleMapper
 * @see Triple
 * @since 0.1.0
 */
public final class TripleMapperImpl<T, T0, T1, T2> extends AbstractTupleMapper<T, Triple<T0, T1, T2>> {

  /**
   * Constructs a new TripleMapperImpl with the specified mapping functions.
   *
   * @param m0 the mapping function for extracting the first element
   * @param m1 the mapping function for extracting the second element
   * @param m2 the mapping function for extracting the third element
   */
  public TripleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2) {
    super(3);
    set(0, m0);
    set(1, m1);
    set(2, m2);
  }

  /**
   * Applies the mapping functions to the source object to create a Triple.
   * <p>
   * This method invokes each of the three mapping functions on the provided
   * source object and combines the results into a new Triple instance.
   * </p>
   *
   * @param t the source object to map
   * @return a new Triple containing the mapped values
   */
  @Override
  public Triple<T0, T1, T2> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t));
  }

  /**
   * Retrieves the mapping function for the first element.
   *
   * @return the function that extracts the first element from the source object
   */
  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  /**
   * Retrieves the mapping function for the second element.
   *
   * @return the function that extracts the second element from the source object
   */
  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }

  /**
   * Retrieves the mapping function for the third element.
   *
   * @return the function that extracts the third element from the source object
   */
  public Function<T, T2> getThird() {
    return getAndCast(2);
  }
}