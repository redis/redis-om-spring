package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.EighthAccessor;
import com.redis.om.spring.tuple.accessor.EleventhAccessor;
import com.redis.om.spring.tuple.accessor.FifteenthAccessor;
import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourteenthAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.NinthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.SeventhAccessor;
import com.redis.om.spring.tuple.accessor.SixthAccessor;
import com.redis.om.spring.tuple.accessor.TenthAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;
import com.redis.om.spring.tuple.accessor.ThirteenthAccessor;
import com.redis.om.spring.tuple.accessor.TwelfthAccessor;

public interface Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> extends Tuple {

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

  E14 getFourteenth();

  E15 getFifteenth();

  @Override
  default int size() {
    return 15;
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
      case 13 -> getFourteenth();
      case 14 -> getFifteenth();
      default -> throw new IndexOutOfBoundsException(
          String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    };
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> FirstAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E1> getFirstGetter() {
    return Quindecuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> SecondAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E2> getSecondGetter() {
    return Quindecuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> ThirdAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E3> getThirdGetter() {
    return Quindecuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> FourthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E4> getFourthGetter() {
    return Quindecuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> FifthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E5> getFifthGetter() {
    return Quindecuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> SixthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E6> getSixthGetter() {
    return Quindecuple::getSixth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> SeventhAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E7> getSeventhGetter() {
    return Quindecuple::getSeventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> EighthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E8> getEighthGetter() {
    return Quindecuple::getEighth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> NinthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E9> getNinthGetter() {
    return Quindecuple::getNinth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> TenthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E10> getTenthGetter() {
    return Quindecuple::getTenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> EleventhAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E11> getEleventhGetter() {
    return Quindecuple::getEleventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> TwelfthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E12> getTwelfthGetter() {
    return Quindecuple::getTwelfth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> ThirteenthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E13> getThirteenthGetter() {
    return Quindecuple::getThirteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> FourteenthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E14> getFourteenthGetter() {
    return Quindecuple::getFourteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15> FifteenthAccessor<Quindecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15>, E15> getFifteenthGetter() {
    return Quindecuple::getFifteenth;
  }
}