package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.EighteenthAccessor;
import com.redis.om.spring.tuple.accessor.EighthAccessor;
import com.redis.om.spring.tuple.accessor.EleventhAccessor;
import com.redis.om.spring.tuple.accessor.FifteenthAccessor;
import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourteenthAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
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

public interface Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18>
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

  @Override
  default int size() {
    return 18;
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
      case 15 -> getSixteenth();
      case 16 -> getSeventeenth();
      case 17 -> getEighteenth();
      default -> throw new IndexOutOfBoundsException(
          String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    };
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> FirstAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E1> getFirstGetter() {
    return Octodecuple::getFirst;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> SecondAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E2> getSecondGetter() {
    return Octodecuple::getSecond;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> ThirdAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E3> getThirdGetter() {
    return Octodecuple::getThird;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> FourthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E4> getFourthGetter() {
    return Octodecuple::getFourth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> FifthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E5> getFifthGetter() {
    return Octodecuple::getFifth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> SixthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E6> getSixthGetter() {
    return Octodecuple::getSixth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> SeventhAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E7> getSeventhGetter() {
    return Octodecuple::getSeventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> EighthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E8> getEighthGetter() {
    return Octodecuple::getEighth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> NinthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E9> getNinthGetter() {
    return Octodecuple::getNinth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> TenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E10> getTenthGetter() {
    return Octodecuple::getTenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> EleventhAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E11> getEleventhGetter() {
    return Octodecuple::getEleventh;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> TwelfthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E12> getTwelfthGetter() {
    return Octodecuple::getTwelfth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> ThirteenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E13> getThirteenthGetter() {
    return Octodecuple::getThirteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> FourteenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E14> getFourteenthGetter() {
    return Octodecuple::getFourteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> FifteenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E15> getFifteenthGetter() {
    return Octodecuple::getFifteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> SixteenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E25> getSixteenthGetter() {
    return Octodecuple::getSixteenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> SeventeenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E26> getSeventeenthGetter() {
    return Octodecuple::getSeventeenth;
  }

  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27> EighteenthAccessor<Octodecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E25, E26, E27>, E27> getEighteenthGetter() {
    return Octodecuple::getEighteenth;
  }
}