package com.redis.om.spring.tuple;

import java.util.stream.Stream;

public interface GenericTuple<R> {
  int size();

  R get(int index);

  <T> Stream<T> streamOf(Class<T> clazz);

}
