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

  T6 get6();

  T7 get7();

  T8 get8();

  T9 get9();

  @Override
  default int size() {
    return 10;
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
        return get6();
      case 7:
        return get7();
      case 8:
        return get8();
      case 9:
        return get9();
      default:
        throw new IndexOutOfBoundsException(
            String.format("Index %d is outside bounds of tuple of degree %s", index, size()));
    }
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> FirstAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T0> getter0() {
    return Decuple::getFirst;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> SecondAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T1> getter1() {
    return Decuple::getSecond;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> ThirdAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T2> getter2() {
    return Decuple::getThird;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> FourthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T3> getter3() {
    return Decuple::getFourth;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> FifthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T4> getter4() {
    return Decuple::getFifth;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> SixthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T5> getter5() {
    return Decuple::getSixth;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> SeventhAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T6> getter6() {
    return Decuple::get6;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> EighthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T7> getter7() {
    return Decuple::get7;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> NinthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T8> getter8() {
    return Decuple::get8;
  }

  static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> TenthAccessor<Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>, T9> getter9() {
    return Decuple::get9;
  }
}