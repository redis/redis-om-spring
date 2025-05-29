package com.redis.om.spring.tuple.impl.mapper;

import java.util.function.Function;

import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Octuple;
import com.redis.om.spring.tuple.Tuples;

/**
 * Implementation of a mapper that transforms objects of type {@code T} into {@link Octuple} instances.
 * <p>
 * This mapper uses eight transformation functions to extract values from the source object
 * and create a tuple containing those values.
 * </p>
 *
 * @param <T>  the source type to be mapped from
 * @param <T0> the type of the first tuple element
 * @param <T1> the type of the second tuple element
 * @param <T2> the type of the third tuple element
 * @param <T3> the type of the fourth tuple element
 * @param <T4> the type of the fifth tuple element
 * @param <T5> the type of the sixth tuple element
 * @param <T6> the type of the seventh tuple element
 * @param <T7> the type of the eighth tuple element
 */
public final class OctupleMapperImpl<T, T0, T1, T2, T3, T4, T5, T6, T7> extends
    AbstractTupleMapper<T, Octuple<T0, T1, T2, T3, T4, T5, T6, T7>> {

  /**
   * Constructs a new OctupleMapperImpl with the specified mapping functions.
   *
   * @param m0 function to extract the first element from the source object
   * @param m1 function to extract the second element from the source object
   * @param m2 function to extract the third element from the source object
   * @param m3 function to extract the fourth element from the source object
   * @param m4 function to extract the fifth element from the source object
   * @param m5 function to extract the sixth element from the source object
   * @param m6 function to extract the seventh element from the source object
   * @param m7 function to extract the eighth element from the source object
   */
  public OctupleMapperImpl(Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3,
      Function<T, T4> m4, Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7) {
    super(8);
    set(0, m0);
    set(1, m1);
    set(2, m2);
    set(3, m3);
    set(4, m4);
    set(5, m5);
    set(6, m6);
    set(7, m7);
  }

  /**
   * Applies the mapping functions to the input object and creates an Octuple.
   *
   * @param t the input object to be mapped
   * @return an Octuple containing the mapped values
   */
  @Override
  public Octuple<T0, T1, T2, T3, T4, T5, T6, T7> apply(T t) {
    return Tuples.of(getFirst().apply(t), getSecond().apply(t), getThird().apply(t), getFourth().apply(t), getFifth()
        .apply(t), getSixth().apply(t), getSeventh().apply(t), getEighth().apply(t));
  }

  /**
   * Returns the mapping function for the first element.
   *
   * @return the mapping function for the first element
   */
  public Function<T, T0> getFirst() {
    return getAndCast(0);
  }

  /**
   * Returns the mapping function for the second element.
   *
   * @return the mapping function for the second element
   */
  public Function<T, T1> getSecond() {
    return getAndCast(1);
  }

  /**
   * Returns the mapping function for the third element.
   *
   * @return the mapping function for the third element
   */
  public Function<T, T2> getThird() {
    return getAndCast(2);
  }

  /**
   * Returns the mapping function for the fourth element.
   *
   * @return the mapping function for the fourth element
   */
  public Function<T, T3> getFourth() {
    return getAndCast(3);
  }

  /**
   * Returns the mapping function for the fifth element.
   *
   * @return the mapping function for the fifth element
   */
  public Function<T, T4> getFifth() {
    return getAndCast(4);
  }

  /**
   * Returns the mapping function for the sixth element.
   *
   * @return the mapping function for the sixth element
   */
  public Function<T, T5> getSixth() {
    return getAndCast(5);
  }

  /**
   * Returns the mapping function for the seventh element.
   *
   * @return the mapping function for the seventh element
   */
  public Function<T, T6> getSeventh() {
    return getAndCast(6);
  }

  /**
   * Returns the mapping function for the eighth element.
   *
   * @return the mapping function for the eighth element
   */
  public Function<T, T7> getEighth() {
    return getAndCast(7);
  }
}