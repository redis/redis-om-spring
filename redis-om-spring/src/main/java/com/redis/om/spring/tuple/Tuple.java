package com.redis.om.spring.tuple;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Tuple extends GenericTuple<Object> {

  default Stream<Object> stream() {
    return IntStream.range(0, size()).mapToObj(this::get);
  }

  @Override
  default <T> Stream<T> streamOf(Class<T> clazz) {
    return stream().filter(clazz::isInstance).map(clazz::cast);
  }
}
