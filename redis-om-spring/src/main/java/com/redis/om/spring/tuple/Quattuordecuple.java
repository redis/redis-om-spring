package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.EighthAccessor;
import com.redis.om.spring.tuple.accessor.EleventhAccessor;
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

public interface Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> extends Tuple {

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

  @Override
  default int size() {
    return 14;
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
      case 9:
        return getTenth();
      case 10:
        return getEleventh();
      case 11:
        return getTwelfth();
      case 12:
        return getThirteenth();
      case 13:
        return getFourteenth();
      default:
        throw new IndexOutOfBoundsException(
            String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    }
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> FirstAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E1> getFirstGetter() {
    return Quattuordecuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> SecondAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E2> getSecondGetter() {
    return Quattuordecuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> ThirdAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E3> getThirdGetter() {
    return Quattuordecuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> FourthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E4> getFourthGetter() {
    return Quattuordecuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> FifthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E5> getFifthGetter() {
    return Quattuordecuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> SixthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E6> getSixthGetter() {
    return Quattuordecuple::getSixth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> SeventhAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E7> getSeventhGetter() {
    return Quattuordecuple::getSeventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> EighthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E8> getEighthGetter() {
    return Quattuordecuple::getEighth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> NinthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E9> getNinthGetter() {
    return Quattuordecuple::getNinth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> TenthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E10> getTenthGetter() {
    return Quattuordecuple::getTenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> EleventhAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E11> getEleventhGetter() {
    return Quattuordecuple::getEleventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> TwelfthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E12> getTwelfthGetter() {
    return Quattuordecuple::getTwelfth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> ThirteenthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E13> getThirteenthGetter() {
    return Quattuordecuple::getThirteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14> FourteenthAccessor<Quattuordecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14>, E14> getFourteenthGetter() {
    return Quattuordecuple::getFourteenth;
  }
}