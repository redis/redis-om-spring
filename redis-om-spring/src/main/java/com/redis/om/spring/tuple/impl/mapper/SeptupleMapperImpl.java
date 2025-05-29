package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Septuple;
import com.redis.om.spring.tuple.Tuples;

/**
 * Mapper implementation for transforming objects into 7-element tuples (septuples).
 * <p>
 * This class provides a concrete implementation of tuple mapping functionality for septuples
 * in the Redis OM Spring framework. It enables transformation of source objects into
 * strongly-typed septuples by applying a set of mapping functions to extract seven
 * different values from the source object.
 * <p>
 * The mapper is commonly used in search operations, aggregations, and data projections
 * where you need to extract multiple fields from entities and present them as structured
 * tuples. Each mapping function corresponds to one position in the resulting septuple.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * // Create a mapper to extract 7 fields from a Person entity
 * SeptupleMapper<Person, String, Integer, String, Date, Boolean, Double, Long> mapper = 
 *     TupleMapper.of(
 *         Person::getName,        // First element
 *         Person::getAge,         // Second element  
 *         Person::getEmail,       // Third element
 *         Person::getBirthDate,   // Fourth element
 *         Person::isActive,       // Fifth element
 *         Person::getSalary,      // Sixth element
 *         Person::getEmployeeId   // Seventh element
 *     );
 * 
 * // Apply the mapper
 * Septuple<String, Integer, String, Date, Boolean, Double, Long> result = mapper.apply(person);
 * }
 * </pre>
 *
 * @param <T>  the type of the source object to be mapped
 * @param <T0> the type of the first element in the resulting septuple
 * @param <T1> the type of the second element in the resulting septuple
 * @param <T2> the type of the third element in the resulting septuple
 * @param <T3> the type of the fourth element in the resulting septuple
 * @param <T4> the type of the fifth element in the resulting septuple
 * @param <T5> the type of the sixth element in the resulting septuple
 * @param <T6> the type of the seventh element in the resulting septuple
 * 
 * @see AbstractTupleMapper
 * @see Septuple
 * @see com.redis.om.spring.tuple.TupleMapper
 * 
 * @author Redis OM Spring Team
 * @since 1.0.0
 */
public final class SeptupleMapperImpl<T, T0, T1, T2, T3, T4, T5, T6> extends
    AbstractTupleMapper<T, Septuple<T0, T1, T2, T3, T4, T5, T6>> {

  /**
   * Constructs a new SeptupleMapperImpl with the specified mapping functions.
   * <p>
   * Creates a tuple mapper that can transform source objects into septuples by applying
   * the provided mapping functions. Each function extracts a specific value from the
   * source object to populate the corresponding position in the resulting septuple.
   * <p>
   * The mapping functions are applied in order when the mapper is invoked, ensuring
   * consistent tuple structure across transformations.
   *
   * @param m0 function to extract the first element from the source object
   * @param m1 function to extract the second element from the source object
   * @param m2 function to extract the third element from the source object
   * @param m3 function to extract the fourth element from the source object
   * @param m4 function to extract the fifth element from the source object
   * @param m5 function to extract the sixth element from the source object
   * @param m6 function to extract the seventh element from the source object
   * 
   * @throws NullPointerException if any mapping function is null
   */
  public SeptupleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3,
      Function<T, T4> m4, Function<T, T5> m5, Function<T, T6> m6) {
    super(7);
    set(0, m0);
    set(1, m1);
    set(2, m2);
    set(3, m3);
    set(4, m4);
    set(5, m5);
    set(6, m6);
  }

  @Override
  public Septuple<T0, T1, T2, T3, T4, T5, T6> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t), getFourth().apply(t), getFifth()
        .apply(t), getSixth().apply(t), getSeventh().apply(t));
  }

  /**
   * Retrieves the mapping function for the first element of the septuple.
   * <p>
   * Returns the function that extracts the first value from source objects
   * when creating septuples.
   *
   * @return the mapping function for the first tuple element
   */
  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  /**
   * Retrieves the mapping function for the second element of the septuple.
   * <p>
   * Returns the function that extracts the second value from source objects
   * when creating septuples.
   *
   * @return the mapping function for the second tuple element
   */
  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }

  /**
   * Retrieves the mapping function for the third element of the septuple.
   * <p>
   * Returns the function that extracts the third value from source objects
   * when creating septuples.
   *
   * @return the mapping function for the third tuple element
   */
  public Function<T, T2> getThird() {
    return getAndCast(2);
  }

  /**
   * Retrieves the mapping function for the fourth element of the septuple.
   * <p>
   * Returns the function that extracts the fourth value from source objects
   * when creating septuples.
   *
   * @return the mapping function for the fourth tuple element
   */
  public Function<T, T3> getFourth() {
    return getAndCast(3);
  }

  /**
   * Retrieves the mapping function for the fifth element of the septuple.
   * <p>
   * Returns the function that extracts the fifth value from source objects
   * when creating septuples.
   *
   * @return the mapping function for the fifth tuple element
   */
  public Function<T, T4> getFifth() {
    return getAndCast(4);
  }

  /**
   * Retrieves the mapping function for the sixth element of the septuple.
   * <p>
   * Returns the function that extracts the sixth value from source objects
   * when creating septuples.
   *
   * @return the mapping function for the sixth tuple element
   */
  public Function<T, T5> getSixth() {
    return getAndCast(5);
  }

  /**
   * Retrieves the mapping function for the seventh element of the septuple.
   * <p>
   * Returns the function that extracts the seventh value from source objects
   * when creating septuples.
   *
   * @return the mapping function for the seventh tuple element
   */
  public Function<T, T6> getSeventh() {
    return getAndCast(6);
  }
}