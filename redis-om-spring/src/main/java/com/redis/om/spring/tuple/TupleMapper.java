package com.redis.om.spring.tuple;

import java.util.function.Function;

/**
 * A functional interface that extends {@link Function} to provide tuple mapping capabilities.
 * TupleMapper is responsible for transforming objects of type T into tuples of type R by applying
 * a collection of mapping functions.
 * 
 * <p>This interface represents a mapper that can extract multiple values from a source object
 * and organize them into a tuple structure. The mapper maintains a collection of individual
 * field extraction functions that are applied to create the resulting tuple.</p>
 * 
 * <p>The degree of a TupleMapper corresponds to the number of elements in the resulting tuple,
 * which equals the number of mapping functions it contains.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * TupleMapper<Person, Triple<String, Integer, String>> mapper = 
 *     new TripleMapper<>(Person::getName, Person::getAge, Person::getEmail);
 * 
 * Person person = new Person("Alice", 30, "alice@example.com");
 * Triple<String, Integer, String> tuple = mapper.apply(person);
 * }</pre>
 * 
 * @param <T> the type of the input object to be mapped
 * @param <R> the type of the resulting tuple
 * 
 * @since 1.0
 * @see Function
 * @see Tuple
 */
public interface TupleMapper<T, R> extends Function<T, R> {
  /**
   * Returns the degree (number of elements) of tuples produced by this mapper.
   * The degree corresponds to the number of mapping functions contained within this mapper.
   * 
   * @return the number of elements in tuples produced by this mapper, always non-negative
   */
  int degree();

  /**
   * Retrieves the mapping function at the specified index.
   * Each mapping function is responsible for extracting one specific value from the source object.
   * 
   * @param index the zero-based index of the mapping function to retrieve
   * @return the mapping function at the specified index
   * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= degree())
   */
  Function<T, ?> get(int index);

}
