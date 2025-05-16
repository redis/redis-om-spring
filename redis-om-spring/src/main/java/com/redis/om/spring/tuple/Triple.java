package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

public interface Triple<E1, E2, E3> extends Tuple {

  static <E1, E2, E3> FirstAccessor<Triple<E1, E2, E3>, E1> getFirstGetter() {
    return Triple::getFirst;
  }

  static <E1, E2, E3> SecondAccessor<Triple<E1, E2, E3>, E2> getSecondGetter() {
    return Triple::getSecond;
  }

  static <E1, E2, E3> ThirdAccessor<Triple<E1, E2, E3>, E3> getThirdGetter() {
    return Triple::getThird;
  }

  E1 getFirst();

  E2 getSecond();

  E3 getThird();

  @Override
  default int size() {
    return 3;
  }

  default Object get(int index) {
    return switch (index) {
      case 0 -> getFirst();
      case 1 -> getSecond();
      case 2 -> getThird();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}