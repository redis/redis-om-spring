
package com.redis.om.spring.tuple.accessor;

import java.util.function.Function;

public interface TupleAccessor<T, R> extends Function<T, R> {
  int index();
}