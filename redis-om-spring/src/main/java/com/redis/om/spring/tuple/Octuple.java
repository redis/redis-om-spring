package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.EighthAccessor;
import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.SeventhAccessor;
import com.redis.om.spring.tuple.accessor.SixthAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

public interface Octuple<E1, E2, E3, E4, E5, E6, E7, T7> extends Tuple {

  E1 getFirst();

  E2 getSecond();

  E3 getThird();

  E4 getFourth();

  E5 getFifth();

  E6 getSixth();

  E7 getSeventh();

  T7 getEighth();

  @Override
  default int size() {
    return 8;
  }

  default Object get(int index) {
    return switch (index) {
      case 0 -> getFirst();
      case 1 -> getSecond();
      case 2 -> getThird();
      case 3 -> getFourth();
      case 4 -> getFifth();
      case 5 -> getSixth();
      case 6 -> getSeventh();
      case 7 -> getEighth();
      default -> throw new IndexOutOfBoundsException(
          String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    };
  }

  static <E1, E2, E3, E4, E5, E6, E7, T7> FirstAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E1> getFirstGetter() {
    return Octuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6, E7, T7> SecondAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E2> getSecondGetter() {
    return Octuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6, E7, T7> ThirdAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E3> getThirdGetter() {
    return Octuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6, E7, T7> FourthAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E4> getFourthGetter() {
    return Octuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, T7> FifthAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E5> getFifthGetter() {
    return Octuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, T7> SixthAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E6> getSixthGetter() {
    return Octuple::getSixth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, T7> SeventhAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, E7> getSeventhGetter() {
    return Octuple::getSeventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, T7> EighthAccessor<Octuple<E1, E2, E3, E4, E5, E6, E7, T7>, T7> getEighthGetter() {
    return Octuple::getEighth;
  }
}