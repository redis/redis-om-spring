package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

public interface Quintuple<E1, E2, E3, E4, E5> extends Tuple {

  E1 getFirst();

  E2 getSecond();

  E3 getThird();

  E4 getFourth();

  E5 getFifth();

  @Override
  default int size() {
    return 5;
  }

  default Object get(int index) {
    return switch (index) {
      case 0 -> getFirst();
      case 1 -> getSecond();
      case 2 -> getThird();
      case 3 -> getFourth();
      case 4 -> getFifth();
      default -> throw new IndexOutOfBoundsException(
          String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    };
  }

  static <E1, E2, E3, E4, E5> FirstAccessor<Quintuple<E1, E2, E3, E4, E5>, E1> getFirstGetter() {
    return Quintuple::getFirst;
  }

  static <E1, E2, E3, E4, E5> SecondAccessor<Quintuple<E1, E2, E3, E4, E5>, E2> getSecondGetter() {
    return Quintuple::getSecond;
  }

  static <E1, E2, E3, E4, E5> ThirdAccessor<Quintuple<E1, E2, E3, E4, E5>, E3> getThirdGetter() {
    return Quintuple::getThird;
  }

  static <E1, E2, E3, E4, E5> FourthAccessor<Quintuple<E1, E2, E3, E4, E5>, E4> getFourthGetter() {
    return Quintuple::getFourth;
  }

  static <E1, E2, E3, E4, E5> FifthAccessor<Quintuple<E1, E2, E3, E4, E5>, E5> getFifthGetter() {
    return Quintuple::getFifth;
  }
}