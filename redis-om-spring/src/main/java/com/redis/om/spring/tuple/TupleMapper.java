package com.redis.om.spring.tuple;

import java.util.function.Function;

public interface TupleMapper<T, R> extends Function<T, R> {
  int degree();

  Function<T, ?> get(int index);

}
