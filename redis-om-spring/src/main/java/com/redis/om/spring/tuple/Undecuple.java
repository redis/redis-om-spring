package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.EighthAccessor;
import com.redis.om.spring.tuple.accessor.EleventhAccessor;
import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.NinthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.SeventhAccessor;
import com.redis.om.spring.tuple.accessor.SixthAccessor;
import com.redis.om.spring.tuple.accessor.TenthAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

public interface Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> extends Tuple {

  E1 getFirst();

  E2 getSecond();

  E3 getThird();

  E4 getFourth();

  E5 getFifth();

  E6 getSixth();

  E7 getSeventh();

  E8 getEighth();

  E9 getNinth();

  E10 getTenth();

  E11 getEleventh();

  @Override
  default int size() {
    return 11;
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
      case 8 -> getNinth();
      case 9 -> getTenth();
      case 10 -> getEleventh();
      default -> throw new IndexOutOfBoundsException(
          String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    };
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> FirstAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E1> getFirstGetter() {
    return Undecuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> SecondAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E2> getSecondGetter() {
    return Undecuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> ThirdAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E3> getThirdGetter() {
    return Undecuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> FourthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E4> getFourthGetter() {
    return Undecuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> FifthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E5> getFifthGetter() {
    return Undecuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> SixthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E6> getSixthGetter() {
    return Undecuple::getSixth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> SeventhAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E7> getSeventhGetter() {
    return Undecuple::getSeventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> EighthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E8> getEighthGetter() {
    return Undecuple::getEighth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> NinthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E9> getNinthGetter() {
    return Undecuple::getNinth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> TenthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E10> getTenthGetter() {
    return Undecuple::getTenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> EleventhAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E11> getEleventhGetter() {
    return Undecuple::getEleventh;
  }
}