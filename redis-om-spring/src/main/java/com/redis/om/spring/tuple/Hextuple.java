package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.SixthAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

public interface Hextuple<E1, E2, E3, E4, E5, E6> extends Tuple {

  E1 getFirst();

  E2 getSecond();

  E3 getThird();

  E4 getFourth();

  E5 getFifth();

  E6 getSixth();

  @Override
  default int size() {
    return 6;
  }

  default Object get(int index) {
    return switch (index) {
      case 0 -> getFirst();
      case 1 -> getSecond();
      case 2 -> getThird();
      case 3 -> getFourth();
      case 4 -> getFifth();
      case 5 -> getSixth();
      default -> throw new IndexOutOfBoundsException(
          String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    };
  }

  static <E1, E2, E3, E4, E5, E6> FirstAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E1> getFirstGetter() {
    return Hextuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6> SecondAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E2> getSecondGetter() {
    return Hextuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6> ThirdAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E3> getThirdGetter() {
    return Hextuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6> FourthAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E4> getFourthGetter() {
    return Hextuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6> FifthAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E5> getFifthGetter() {
    return Hextuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6> SixthAccessor<Hextuple<E1, E2, E3, E4, E5, E6>, E6> getSixthGetter() {
    return Hextuple::getSixth;
  }
}