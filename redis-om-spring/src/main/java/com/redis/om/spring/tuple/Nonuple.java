package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.EighthAccessor;
import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.NinthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.SeventhAccessor;
import com.redis.om.spring.tuple.accessor.SixthAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

public interface Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9> extends Tuple {

  E1 getFirst();

  E2 getSecond();

  E3 getThird();

  E4 getFourth();

  E5 getFifth();

  E6 getSixth();

  E7 getSeventh();

  E8 getEighth();

  E9 getNinth();

  @Override
  default int size() {
    return 9;
  }

  default Object get(int index) {
    switch (index) {
      case 0:
        return getFirst();
      case 1:
        return getSecond();
      case 2:
        return getThird();
      case 3:
        return getFourth();
      case 4:
        return getFifth();
      case 5:
        return getSixth();
      case 6:
        return getSeventh();
      case 7:
        return getEighth();
      case 8:
        return getNinth();
      default:
        throw new IndexOutOfBoundsException(
            String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    }
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> FirstAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E1> getFirstGetter() {
    return Nonuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> SecondAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E2> getSecondGetter() {
    return Nonuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> ThirdAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E3> getThirdGetter() {
    return Nonuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> FourthAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E4> getFourthGetter() {
    return Nonuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> FifthAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E5> getFifthGetter() {
    return Nonuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> SixthAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E6> getSixthGetter() {
    return Nonuple::getSixth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> SeventhAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E7> getSeventhGetter() {
    return Nonuple::getSeventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> EighthAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E8> getEighthGetter() {
    return Nonuple::getEighth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9> NinthAccessor<Nonuple<E1, E2, E3, E4, E5, E6, E7, E8, E9>, E9> getNinthGetter() {
    return Nonuple::getNinth;
  }
}