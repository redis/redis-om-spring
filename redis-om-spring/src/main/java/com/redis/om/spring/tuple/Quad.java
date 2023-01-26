package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

public interface Quad<E1, E2, E3, E4> extends Tuple {

  E1 getFirst();

  E2 getSecond();

  E3 getThird();

  E4 getFourth();

  @Override
  default int size() {
    return 4;
  }

  default Object get(int index) {
    return switch (index) {
      case 0 -> getFirst();
      case 1 -> getSecond();
      case 2 -> getThird();
      case 3 -> getFourth();
      default -> throw new IndexOutOfBoundsException(
          String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    };
  }

  static <E1, E2, E3, E4> FirstAccessor<Quad<E1, E2, E3, E4>, E1> getFirstGetter() {
    return Quad::getFirst;
  }

  static <E1, E2, E3, E4> SecondAccessor<Quad<E1, E2, E3, E4>, E2> getSecondGetter() {
    return Quad::getSecond;
  }

  static <E1, E2, E3, E4> ThirdAccessor<Quad<E1, E2, E3, E4>, E3> getThirdGetter() {
    return Quad::getThird;
  }

  static <E1, E2, E3, E4> FourthAccessor<Quad<E1, E2, E3, E4>, E4> getFourthGetter() {
    return Quad::getFourth;
  }
}