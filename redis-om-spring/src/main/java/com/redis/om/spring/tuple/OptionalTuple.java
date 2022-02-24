package com.redis.om.spring.tuple;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.redis.om.spring.tuple.accessor.TupleAccessor;

public interface OptionalTuple extends GenericTuple<Optional<Object>> {

  default Stream<Optional<Object>> stream() {
    return IntStream.range(0, size()).mapToObj(this::get);
  }

  @Override
  default <T> Stream<T> streamOf(Class<T> clazz) {
    return stream().filter(Optional::isPresent).map(Optional::get).filter(clazz::isInstance).map(clazz::cast);
  }

  static <TUPLE extends OptionalTuple, R> TupleAccessor<TUPLE, R> getter(int index) {
    return new TupleAccessor<TUPLE, R>() {
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
