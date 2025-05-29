package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Decuple;
import com.redis.om.spring.tuple.Tuples;

/**
 * Implementation of a tuple mapper that converts objects of type T into Decuple tuples.
 * This class provides the concrete implementation for mapping source objects to ten-element tuples
 * using configurable extraction functions for each tuple position.
 * 
 * <p>This mapper is commonly used in Redis OM Spring for:
 * <ul>
 * <li>Converting query results into structured tuple representations</li>
 * <li>Projecting entity fields into tuple form for aggregation operations</li>
 * <li>Transforming complex objects into simpler ten-element data structures</li>
 * <li>Supporting functional programming patterns in data transformation pipelines</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * DecupleMapperImpl<Person, String, Integer, String, Date, Boolean, Double, String, Long, Float, Character> mapper =
 *     new DecupleMapperImpl<>(
 *         Person::getName,
 *         Person::getAge,
 *         Person::getEmail,
 *         Person::getBirthDate,
 *         Person::isActive,
 *         Person::getSalary,
 *         Person::getDepartment,
 *         Person::getEmployeeId,
 *         Person::getHeight,
 *         Person::getGrade
 *     );
 * 
 * Decuple<String, Integer, String, Date, Boolean, Double, String, Long, Float, Character> result = 
 *     mapper.apply(person);
 * }
 * </pre>
 *
 * @param <T>  the source object type to be mapped from
 * @param <T0> the type of the first tuple element
 * @param <T1> the type of the second tuple element
 * @param <T2> the type of the third tuple element
 * @param <T3> the type of the fourth tuple element
 * @param <T4> the type of the fifth tuple element
 * @param <T5> the type of the sixth tuple element
 * @param <T6> the type of the seventh tuple element
 * @param <T7> the type of the eighth tuple element
 * @param <T8> the type of the ninth tuple element
 * @param <T9> the type of the tenth tuple element
 * 
 * @author Redis OM Spring Development Team
 * @since 0.8.0
 */
public final class DecupleMapperImpl<T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> extends
    AbstractTupleMapper<T, Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>> {

  /**
   * Constructs a new DecupleMapperImpl with the specified extraction functions.
   * Each function parameter corresponds to the extractor for the respective tuple position.
   * 
   * @param m0 function to extract the first element from source objects
   * @param m1 function to extract the second element from source objects
   * @param m2 function to extract the third element from source objects
   * @param m3 function to extract the fourth element from source objects
   * @param m4 function to extract the fifth element from source objects
   * @param m5 function to extract the sixth element from source objects
   * @param m6 function to extract the seventh element from source objects
   * @param m7 function to extract the eighth element from source objects
   * @param m8 function to extract the ninth element from source objects
   * @param m9 function to extract the tenth element from source objects
   * @throws NullPointerException if any of the mapper functions is null
   */
  public DecupleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3,
      Function<T, T4> m4, Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8,
      Function<T, T9> m9) {
    super(10);
    set(0, m0);
    set(1, m1);
    set(2, m2);
    set(3, m3);
    set(4, m4);
    set(5, m5);
    set(6, m6);
    set(7, m7);
    set(8, m8);
    set(9, m9);
  }

  /**
   * Applies this mapper to the given source object, producing a Decuple tuple.
   * This method executes all configured extraction functions against the source object
   * and constructs a new Decuple containing the results.
   * 
   * @param t the source object to map from
   * @return a new Decuple containing the extracted values
   * @throws NullPointerException if the source object is null and any extraction function doesn't handle nulls
   */
  @Override
  public Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t), getFourth().apply(t), getFifth()
        .apply(t), getSixth().apply(t), getSeventh().apply(t), getEighth().apply(t), getNinth().apply(t), getTenth()
            .apply(t));
  }

  /**
   * Gets the extraction function for the first tuple element.
   * 
   * @return the function that extracts the first element from source objects
   */
  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  /**
   * Gets the extraction function for the second tuple element.
   * 
   * @return the function that extracts the second element from source objects
   */
  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }

  /**
   * Gets the extraction function for the third tuple element.
   * 
   * @return the function that extracts the third element from source objects
   */
  public Function<T, T2> getThird() {
    return getAndCast(2);
  }

  /**
   * Gets the extraction function for the fourth tuple element.
   * 
   * @return the function that extracts the fourth element from source objects
   */
  public Function<T, T3> getFourth() {
    return getAndCast(3);
  }

  /**
   * Gets the extraction function for the fifth tuple element.
   * 
   * @return the function that extracts the fifth element from source objects
   */
  public Function<T, T4> getFifth() {
    return getAndCast(4);
  }

  /**
   * Gets the extraction function for the sixth tuple element.
   * 
   * @return the function that extracts the sixth element from source objects
   */
  public Function<T, T5> getSixth() {
    return getAndCast(5);
  }

  /**
   * Gets the extraction function for the seventh tuple element.
   * 
   * @return the function that extracts the seventh element from source objects
   */
  public Function<T, T6> getSeventh() {
    return getAndCast(6);
  }

  /**
   * Gets the extraction function for the eighth tuple element.
   * 
   * @return the function that extracts the eighth element from source objects
   */
  public Function<T, T7> getEighth() {
    return getAndCast(7);
  }

  /**
   * Gets the extraction function for the ninth tuple element.
   * 
   * @return the function that extracts the ninth element from source objects
   */
  public Function<T, T8> getNinth() {
    return getAndCast(8);
  }

  /**
   * Gets the extraction function for the tenth tuple element.
   * 
   * @return the function that extracts the tenth element from source objects
   */
  public Function<T, T9> getTenth() {
    return getAndCast(9);
  }
}