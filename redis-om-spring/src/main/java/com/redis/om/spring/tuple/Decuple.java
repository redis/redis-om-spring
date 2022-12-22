package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.EighthAccessor;
import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.NinthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.SeventhAccessor;
import com.redis.om.spring.tuple.accessor.SixthAccessor;
import com.redis.om.spring.tuple.accessor.TenthAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

public interface Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> extends Tuple {

  T0 getFirst();

  T1 getSecond();

  T2 getThird();

  T3 getFourth();

  T4 getFifth();

  T5 getSixth();

  T6 getSeventh();

  T7 getEighth();

  T8 getNinth();

  T9 getTenth();

  @Override
  default int size() {
    return 10;
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
      default -> throw new IndexOutOfBoundsException(
          String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    };
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> FirstAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T0> getFirstGetter() {
    return Decuple::getFirst;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> SecondAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T1> getSecondGetter() {
    return Decuple::getSecond;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> ThirdAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T2> getThirdGetter() {
    return Decuple::getThird;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> FourthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T3> getFourthGetter() {
    return Decuple::getFourth;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> FifthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T4> getFifthGetter() {
    return Decuple::getFifth;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> SixthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T5> getSixthGetter() {
    return Decuple::getSixth;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> SeventhAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T6> getSeventhGetter() {
    return Decuple::getSeventh;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> EighthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T7> getEighthGetter() {
    return Decuple::getEighth;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> NinthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T8> getNinthGetter() {
    return Decuple::getNinth;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> TenthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T9> getTenthGetter() {
    return Decuple::getTenth;
  }
}