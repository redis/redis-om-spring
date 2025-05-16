package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

public interface Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> extends Tuple {

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> FirstAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E1> getFirstGetter() {
    return Tredecuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> SecondAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E2> getSecondGetter() {
    return Tredecuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> ThirdAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E3> getThirdGetter() {
    return Tredecuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> FourthAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E4> getFourthGetter() {
    return Tredecuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> FifthAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E5> getFifthGetter() {
    return Tredecuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> SixthAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E6> getSixthGetter() {
    return Tredecuple::getSixth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> SeventhAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E7> getSeventhGetter() {
    return Tredecuple::getSeventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> EighthAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E8> getEighthGetter() {
    return Tredecuple::getEighth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> NinthAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E9> getNinthGetter() {
    return Tredecuple::getNinth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> TenthAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E10> getTenthGetter() {
    return Tredecuple::getTenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> EleventhAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E11> getEleventhGetter() {
    return Tredecuple::getEleventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> TwelfthAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E12> getTwelfthGetter() {
    return Tredecuple::getTwelfth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13> ThirteenthAccessor<Tredecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13>, E13> getThirteenthGetter() {
    return Tredecuple::getThirteenth;
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

  E13 getThirteenth();

  @Override
  default int size() {
    return 13;
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
      case 12 -> getThirteenth();
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}