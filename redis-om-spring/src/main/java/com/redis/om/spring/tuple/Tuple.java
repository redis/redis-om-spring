package com.redis.om.spring.tuple;

import java.util.Collections;
import java.util.Map;
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

  @Override
  default Map<String, Object> labelledMap() {
    return Collections.emptyMap();
  }
}
