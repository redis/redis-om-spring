package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.EighteenthAccessor;
import com.redis.om.spring.tuple.accessor.EighthAccessor;
import com.redis.om.spring.tuple.accessor.EleventhAccessor;
import com.redis.om.spring.tuple.accessor.FifteenthAccessor;
import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourteenthAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.NineteenthAccessor;
import com.redis.om.spring.tuple.accessor.NinthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.SeventeenthAccessor;
import com.redis.om.spring.tuple.accessor.SeventhAccessor;
import com.redis.om.spring.tuple.accessor.SixteenthAccessor;
import com.redis.om.spring.tuple.accessor.SixthAccessor;
import com.redis.om.spring.tuple.accessor.TenthAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;
import com.redis.om.spring.tuple.accessor.ThirteenthAccessor;
import com.redis.om.spring.tuple.accessor.TwelfthAccessor;
import com.redis.om.spring.tuple.accessor.TwentiethAccessor;

public interface Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>
    extends Tuple {

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

  E16 getSixteenth();

  E17 getSeventeenth();

  E18 getEighteenth();

  E19 getNineteenth();

  E20 getTwentieth();

  @Override
  default int size() {
    return 20;
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
      case 14:
        return getFifteenth();
      case 15:
        return getSixteenth();
      case 16:
        return getSeventeenth();
      case 17:
        return getEighteenth();
      case 18:
        return getNineteenth();
      case 19:
        return getTwentieth();
      default:
        throw new IndexOutOfBoundsException(
            String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    }
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> FirstAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E1> getFirstGetter() {
    return Vigintuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> SecondAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E2> getSecondGetter() {
    return Vigintuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> ThirdAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E3> getThirdGetter() {
    return Vigintuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> FourthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E4> getFourthGetter() {
    return Vigintuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> FifthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E5> getFifthGetter() {
    return Vigintuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> SixthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E6> getSixthGetter() {
    return Vigintuple::getSixth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> SeventhAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E7> getSeventhGetter() {
    return Vigintuple::getSeventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> EighthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E8> getEighthGetter() {
    return Vigintuple::getEighth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> NinthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E9> getNinthGetter() {
    return Vigintuple::getNinth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> TenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E10> getTenthGetter() {
    return Vigintuple::getTenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> EleventhAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E11> getEleventhGetter() {
    return Vigintuple::getEleventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> TwelfthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E12> getTwelfthGetter() {
    return Vigintuple::getTwelfth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> ThirteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E13> getThirteenthGetter() {
    return Vigintuple::getThirteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> FourteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E14> getFourteenthGetter() {
    return Vigintuple::getFourteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> FifteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E15> getFifteenthGetter() {
    return Vigintuple::getFifteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> SixteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E16> getSixteenthGetter() {
    return Vigintuple::getSixteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> SeventeenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E17> getSeventeenthGetter() {
    return Vigintuple::getSeventeenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> EighteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E18> getEighteenthGetter() {
    return Vigintuple::getEighteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> NineteenthAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E19> getNineteenthGetter() {
    return Vigintuple::getNineteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20> TwentiethAccessor<Vigintuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20>, E20> getTwentiethGetter() {
    return Vigintuple::getTwentieth;
  }
}