
package com.redis.om.spring.tuple;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.redis.om.spring.tuple.getter.TupleGetter;

/**
 * This interface defines a generic Tuple of any order that can hold null
 * values. A Tuple is type safe, immutable and thread safe. For pure non-null
 * value elements see {@link Tuple}
 *
 * @see Tuple
 *
 */
public interface MutableTuple extends BasicTuple<Optional<Object>> {

  /**
   * Returns a {@link Stream} of all values for this Tuple. If sequential, the
   * Stream will start with the 0:th tuple and progress upwards.
   *
   * @return a {@link Stream} of all values for this Tuple
   */
  default Stream<Optional<Object>> stream() {
    return IntStream.range(0, size()).mapToObj(this::get);
  }

  @Override
  default <T> Stream<T> streamOf(Class<T> clazz) {
    return stream().filter(Optional::isPresent).map(Optional::get).filter(clazz::isInstance).map(clazz::cast);
  }

  /**
   * Returns a getter method for the specified ordinal element.
   *
   * @param index   the position of the element to return
   * @param <TUPLE> the type of the tuple
   * @param <R>     the type of the returned element
   * @return the created getter
   */
  static <TUPLE extends MutableTuple, R> TupleGetter<TUPLE, R> getter(int index) {
    return new TupleGetter<TUPLE, R>() {
      @Override
      public int index() {
        return index;
      }

      @Override
      @SuppressWarnings("unchecked")
      public R apply(TUPLE tuple) {
        return (R) tuple.get(index);
      }
    };
  }

}
