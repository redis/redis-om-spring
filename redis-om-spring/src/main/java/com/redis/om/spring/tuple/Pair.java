package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;

public interface Pair<E1, E2> extends Tuple {

  E1 getFirst();

  E2 getSecond();

  @Override
  default int size() {
    return 2;
  }

  default Object get(int index) {
    switch (index) {
      case 0:
        return getFirst();
      case 1:
        return getSecond();
      default:
        throw new IndexOutOfBoundsException(
            String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    }
  }

  static <E1, E2> FirstAccessor<Pair<E1, E2>, E1> getFirstGetter() {
    return Pair::getFirst;
  }

  static <E1, E2> SecondAccessor<Pair<E1, E2>, E2> getSecondGetter() {
    return Pair::getSecond;
  }
}