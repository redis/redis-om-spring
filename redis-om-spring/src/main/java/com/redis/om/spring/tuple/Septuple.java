package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.SeventhAccessor;
import com.redis.om.spring.tuple.accessor.SixthAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

public interface Septuple<E1, E2, E3, E4, E5, E6, E7> extends Tuple {

  E1 getFirst();

  E2 getSecond();

  E3 getThird();

  E4 getFourth();

  E5 getFifth();

  E6 getSixth();

  E7 getSeventh();

  @Override
  default int size() {
    return 7;
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
      default -> throw new IndexOutOfBoundsException(
          String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    };
  }

  static <E1, E2, E3, E4, E5, E6, E7> FirstAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E1> getFirstGetter() {
    return Septuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6, E7> SecondAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E2> getSecondGetter() {
    return Septuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6, E7> ThirdAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E3> getThirdGetter() {
    return Septuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6, E7> FourthAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E4> getFourthGetter() {
    return Septuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6, E7> FifthAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E5> getFifthGetter() {
    return Septuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6, E7> SixthAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E6> getSixthGetter() {
    return Septuple::getSixth;
  }

  static <E1, E2, E3, E4, E5, E6, E7> SeventhAccessor<Septuple<E1, E2, E3, E4, E5, E6, E7>, E7> getSeventhGetter() {
    return Septuple::getSeventh;
  }
}