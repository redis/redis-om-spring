package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FirstAccessor;

public interface Single<T0> extends Tuple {

  static <T0> FirstAccessor<Single<T0>, T0> getFirstGetter() {
    return Single::getFirst;
  }

  T0 getFirst();

  @Override
  default int size() {
    return 1;
  }

  default Object get(int index) {
    if (index == 0) {
      return getFirst();
    } else {
      throw new IndexOutOfBoundsException(
        String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    }
  }
}