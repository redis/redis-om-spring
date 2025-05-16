package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

public interface Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> extends Tuple {

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> FirstAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E1> getFirstGetter() {
    return Duodecuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> SecondAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E2> getSecondGetter() {
    return Duodecuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> ThirdAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E3> getThirdGetter() {
    return Duodecuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> FourthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E4> getFourthGetter() {
    return Duodecuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> FifthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E5> getFifthGetter() {
    return Duodecuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> SixthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E6> getSixthGetter() {
    return Duodecuple::getSixth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> SeventhAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E7> getSeventhGetter() {
    return Duodecuple::getSeventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> EighthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E8> getEighthGetter() {
    return Duodecuple::getEighth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> NinthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E9> getNinthGetter() {
    return Duodecuple::getNinth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> TenthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E10> getTenthGetter() {
    return Duodecuple::getTenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> EleventhAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E11> getEleventhGetter() {
    return Duodecuple::getEleventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12> TwelfthAccessor<Duodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12>, E12> getTwelfthGetter() {
    return Duodecuple::getTwelfth;
  }

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

  E12 getTwelfth();

  @Override
  default int size() {
    return 12;
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
      case 11 -> getTwelfth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}