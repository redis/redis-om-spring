package com.redis.om.spring.tuple;

import java.util.function.Function;

import com.redis.om.spring.tuple.impl.mapper.*;

/**
 * Utility class for creating tuple field mappers.
 */
public final class Fields {

  private Fields() {
  }

  /**
   * Creates empty tuple mapper.
   *
   * @param <T> the input type
   * @return a function that maps any input to an empty tuple
   */
  @SuppressWarnings(
    "unchecked"
  )
  public static <T> Function<T, EmptyTuple> of() {
    return (Function<T, EmptyTuple>) EmptyTupleMapperImpl.EMPTY_MAPPER;
  }

  /**
   * Creates single field tuple mapper.
   * 
   * @param <T>  source type
   * @param <T0> first field type
   * @param m0   first field mapper
   * @return single tuple mapper
   */
  public static <T, T0> Function<T, Single<T0>> of(Function<T, T0> m0) {
    return new SingleMapperImpl<>(m0);
  }

  /**
   * Creates two-field tuple mapper.
   * 
   * @param <T>  source type
   * @param <T0> first field type
   * @param <T1> second field type
   * @param m0   first field mapper
   * @param m1   second field mapper
   * @return pair tuple mapper
   */
  public static <T, T0, T1> Function<T, Pair<T0, T1>> of(Function<T, T0> m0, Function<T, T1> m1) {
    return new PairMapperImpl<>(m0, m1);
  }

  /**
   * Creates three-field tuple mapper.
   * 
   * @param <T>  source type
   * @param <T0> first field type
   * @param <T1> second field type
   * @param <T2> third field type
   * @param m0   first field mapper
   * @param m1   second field mapper
   * @param m2   third field mapper
   * @return triple tuple mapper
   */
  public static <T, T0, T1, T2> Function<T, Triple<T0, T1, T2>> of(Function<T, T0> m0, Function<T, T1> m1,
      Function<T, T2> m2) {
    return new TripleMapperImpl<>(m0, m1, m2);
  }

  /**
   * Creates four-field tuple mapper.
   * 
   * @param <T>  source type
   * @param <T0> first field type
   * @param <T1> second field type
   * @param <T2> third field type
   * @param <T3> fourth field type
   * @param m0   first field mapper
   * @param m1   second field mapper
   * @param m2   third field mapper
   * @param m3   fourth field mapper
   * @return quad tuple mapper
   */
  public static <T, T0, T1, T2, T3> Function<T, Quad<T0, T1, T2, T3>> of(Function<T, T0> m0, Function<T, T1> m1,
      Function<T, T2> m2, Function<T, T3> m3) {
    return new QuadMapperImpl<>(m0, m1, m2, m3);
  }

  /**
   * Creates five-field tuple mapper.
   * 
   * @param <T>  source type
   * @param <T0> first field type
   * @param <T1> second field type
   * @param <T2> third field type
   * @param <T3> fourth field type
   * @param <T4> fifth field type
   * @param m0   first field mapper
   * @param m1   second field mapper
   * @param m2   third field mapper
   * @param m3   fourth field mapper
   * @param m4   fifth field mapper
   * @return quintuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4> Function<T, Quintuple<T0, T1, T2, T3, T4>> of(Function<T, T0> m0,
      Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4) {
    return new QuintupleMapperImpl<>(m0, m1, m2, m3, m4);
  }

  /**
   * Creates six-field tuple mapper.
   * 
   * @param <T>  source type
   * @param <T0> first field type
   * @param <T1> second field type
   * @param <T2> third field type
   * @param <T3> fourth field type
   * @param <T4> fifth field type
   * @param <T5> sixth field type
   * @param m0   first field mapper
   * @param m1   second field mapper
   * @param m2   third field mapper
   * @param m3   fourth field mapper
   * @param m4   fifth field mapper
   * @param m5   sixth field mapper
   * @return hextuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5> Function<T, Hextuple<T0, T1, T2, T3, T4, T5>> of(Function<T, T0> m0,
      Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4, Function<T, T5> m5) {
    return new HextupleMapperImpl<>(m0, m1, m2, m3, m4, m5);
  }

  /**
   * Creates seven-field tuple mapper.
   * 
   * @param <T>  source type
   * @param <T0> first field type
   * @param <T1> second field type
   * @param <T2> third field type
   * @param <T3> fourth field type
   * @param <T4> fifth field type
   * @param <T5> sixth field type
   * @param <T6> seventh field type
   * @param m0   first field mapper
   * @param m1   second field mapper
   * @param m2   third field mapper
   * @param m3   fourth field mapper
   * @param m4   fifth field mapper
   * @param m5   sixth field mapper
   * @param m6   seventh field mapper
   * @return septuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6> Function<T, Septuple<T0, T1, T2, T3, T4, T5, T6>> of(Function<T, T0> m0,
      Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4, Function<T, T5> m5,
      Function<T, T6> m6) {
    return new SeptupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6);
  }

  /**
   * Creates eight-field tuple mapper.
   * 
   * @param <T>  source type
   * @param <T0> first field type
   * @param <T1> second field type
   * @param <T2> third field type
   * @param <T3> fourth field type
   * @param <T4> fifth field type
   * @param <T5> sixth field type
   * @param <T6> seventh field type
   * @param <T7> eighth field type
   * @param m0   first field mapper
   * @param m1   second field mapper
   * @param m2   third field mapper
   * @param m3   fourth field mapper
   * @param m4   fifth field mapper
   * @param m5   sixth field mapper
   * @param m6   seventh field mapper
   * @param m7   eighth field mapper
   * @return octuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7> Function<T, Octuple<T0, T1, T2, T3, T4, T5, T6, T7>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7) {
    return new OctupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7);
  }

  /**
   * Creates nine-field tuple mapper.
   * 
   * @param <T>  source type
   * @param <T0> first field type
   * @param <T1> second field type
   * @param <T2> third field type
   * @param <T3> fourth field type
   * @param <T4> fifth field type
   * @param <T5> sixth field type
   * @param <T6> seventh field type
   * @param <T7> eighth field type
   * @param <T8> ninth field type
   * @param m0   first field mapper
   * @param m1   second field mapper
   * @param m2   third field mapper
   * @param m3   fourth field mapper
   * @param m4   fifth field mapper
   * @param m5   sixth field mapper
   * @param m6   seventh field mapper
   * @param m7   eighth field mapper
   * @param m8   ninth field mapper
   * @return nonuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8> Function<T, Nonuple<T0, T1, T2, T3, T4, T5, T6, T7, T8>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8) {
    return new NonupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8);
  }

  /**
   * Creates ten-field tuple mapper.
   * 
   * @param <T>  source type
   * @param <T0> first field type
   * @param <T1> second field type
   * @param <T2> third field type
   * @param <T3> fourth field type
   * @param <T4> fifth field type
   * @param <T5> sixth field type
   * @param <T6> seventh field type
   * @param <T7> eighth field type
   * @param <T8> ninth field type
   * @param <T9> tenth field type
   * @param m0   first field mapper
   * @param m1   second field mapper
   * @param m2   third field mapper
   * @param m3   fourth field mapper
   * @param m4   fifth field mapper
   * @param m5   sixth field mapper
   * @param m6   seventh field mapper
   * @param m7   eighth field mapper
   * @param m8   ninth field mapper
   * @param m9   tenth field mapper
   * @return decuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> Function<T, Decuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9) {
    return new DecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9);
  }

  /**
   * Creates eleven-field tuple mapper.
   * 
   * @param <T>   source type
   * @param <T0>  first field type
   * @param <T1>  second field type
   * @param <T2>  third field type
   * @param <T3>  fourth field type
   * @param <T4>  fifth field type
   * @param <T5>  sixth field type
   * @param <T6>  seventh field type
   * @param <T7>  eighth field type
   * @param <T8>  ninth field type
   * @param <T9>  tenth field type
   * @param <T10> eleventh field type
   * @param m0    first field mapper
   * @param m1    second field mapper
   * @param m2    third field mapper
   * @param m3    fourth field mapper
   * @param m4    fifth field mapper
   * @param m5    sixth field mapper
   * @param m6    seventh field mapper
   * @param m7    eighth field mapper
   * @param m8    ninth field mapper
   * @param m9    tenth field mapper
   * @param m10   eleventh field mapper
   * @return undecuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Function<T, Undecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10) {
    return new UndecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10);
  }

  /**
   * Creates twelve-field tuple mapper.
   * 
   * @param <T>   source type
   * @param <T0>  first field type
   * @param <T1>  second field type
   * @param <T2>  third field type
   * @param <T3>  fourth field type
   * @param <T4>  fifth field type
   * @param <T5>  sixth field type
   * @param <T6>  seventh field type
   * @param <T7>  eighth field type
   * @param <T8>  ninth field type
   * @param <T9>  tenth field type
   * @param <T10> eleventh field type
   * @param <T11> twelfth field type
   * @param m0    first field mapper
   * @param m1    second field mapper
   * @param m2    third field mapper
   * @param m3    fourth field mapper
   * @param m4    fifth field mapper
   * @param m5    sixth field mapper
   * @param m6    seventh field mapper
   * @param m7    eighth field mapper
   * @param m8    ninth field mapper
   * @param m9    tenth field mapper
   * @param m10   eleventh field mapper
   * @param m11   twelfth field mapper
   * @return duodecuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Function<T, Duodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11) {
    return new DuodecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11);
  }

  /**
   * Creates thirteen-field tuple mapper.
   * 
   * @param <T>   source type
   * @param <T0>  first field type
   * @param <T1>  second field type
   * @param <T2>  third field type
   * @param <T3>  fourth field type
   * @param <T4>  fifth field type
   * @param <T5>  sixth field type
   * @param <T6>  seventh field type
   * @param <T7>  eighth field type
   * @param <T8>  ninth field type
   * @param <T9>  tenth field type
   * @param <T10> eleventh field type
   * @param <T11> twelfth field type
   * @param <T12> thirteenth field type
   * @param m0    first field mapper
   * @param m1    second field mapper
   * @param m2    third field mapper
   * @param m3    fourth field mapper
   * @param m4    fifth field mapper
   * @param m5    sixth field mapper
   * @param m6    seventh field mapper
   * @param m7    eighth field mapper
   * @param m8    ninth field mapper
   * @param m9    tenth field mapper
   * @param m10   eleventh field mapper
   * @param m11   twelfth field mapper
   * @param m12   thirteenth field mapper
   * @return tredecuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Function<T, Tredecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12) {
    return new TredecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12);
  }

  /**
   * Creates fourteen-field tuple mapper.
   * 
   * @param <T>   source type
   * @param <T0>  first field type
   * @param <T1>  second field type
   * @param <T2>  third field type
   * @param <T3>  fourth field type
   * @param <T4>  fifth field type
   * @param <T5>  sixth field type
   * @param <T6>  seventh field type
   * @param <T7>  eighth field type
   * @param <T8>  ninth field type
   * @param <T9>  tenth field type
   * @param <T10> eleventh field type
   * @param <T11> twelfth field type
   * @param <T12> thirteenth field type
   * @param <T13> fourteenth field type
   * @param m0    first field mapper
   * @param m1    second field mapper
   * @param m2    third field mapper
   * @param m3    fourth field mapper
   * @param m4    fifth field mapper
   * @param m5    sixth field mapper
   * @param m6    seventh field mapper
   * @param m7    eighth field mapper
   * @param m8    ninth field mapper
   * @param m9    tenth field mapper
   * @param m10   eleventh field mapper
   * @param m11   twelfth field mapper
   * @param m12   thirteenth field mapper
   * @param m13   fourteenth field mapper
   * @return quattuordecuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Function<T, Quattuordecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13) {
    return new QuattuordecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13);
  }

  /**
   * Creates fifteen-field tuple mapper.
   * 
   * @param <T>   source type
   * @param <T0>  first field type
   * @param <T1>  second field type
   * @param <T2>  third field type
   * @param <T3>  fourth field type
   * @param <T4>  fifth field type
   * @param <T5>  sixth field type
   * @param <T6>  seventh field type
   * @param <T7>  eighth field type
   * @param <T8>  ninth field type
   * @param <T9>  tenth field type
   * @param <T10> eleventh field type
   * @param <T11> twelfth field type
   * @param <T12> thirteenth field type
   * @param <T13> fourteenth field type
   * @param <T14> fifteenth field type
   * @param m0    first field mapper
   * @param m1    second field mapper
   * @param m2    third field mapper
   * @param m3    fourth field mapper
   * @param m4    fifth field mapper
   * @param m5    sixth field mapper
   * @param m6    seventh field mapper
   * @param m7    eighth field mapper
   * @param m8    ninth field mapper
   * @param m9    tenth field mapper
   * @param m10   eleventh field mapper
   * @param m11   twelfth field mapper
   * @param m12   thirteenth field mapper
   * @param m13   fourteenth field mapper
   * @param m14   fifteenth field mapper
   * @return quindecuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Function<T, Quindecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14) {
    return new QuindecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14);
  }

  /**
   * Creates sixteen-field tuple mapper.
   * 
   * @param <T>   source type
   * @param <T0>  first field type
   * @param <T1>  second field type
   * @param <T2>  third field type
   * @param <T3>  fourth field type
   * @param <T4>  fifth field type
   * @param <T5>  sixth field type
   * @param <T6>  seventh field type
   * @param <T7>  eighth field type
   * @param <T8>  ninth field type
   * @param <T9>  tenth field type
   * @param <T10> eleventh field type
   * @param <T11> twelfth field type
   * @param <T12> thirteenth field type
   * @param <T13> fourteenth field type
   * @param <T14> fifteenth field type
   * @param <T15> sixteenth field type
   * @param m0    first field mapper
   * @param m1    second field mapper
   * @param m2    third field mapper
   * @param m3    fourth field mapper
   * @param m4    fifth field mapper
   * @param m5    sixth field mapper
   * @param m6    seventh field mapper
   * @param m7    eighth field mapper
   * @param m8    ninth field mapper
   * @param m9    tenth field mapper
   * @param m10   eleventh field mapper
   * @param m11   twelfth field mapper
   * @param m12   thirteenth field mapper
   * @param m13   fourteenth field mapper
   * @param m14   fifteenth field mapper
   * @param m15   sixteenth field mapper
   * @return sexdecuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Function<T, Sexdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15) {
    return new SexdecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15);
  }

  /**
   * Creates seventeen-field tuple mapper.
   * 
   * @param <T>   source type
   * @param <T0>  first field type
   * @param <T1>  second field type
   * @param <T2>  third field type
   * @param <T3>  fourth field type
   * @param <T4>  fifth field type
   * @param <T5>  sixth field type
   * @param <T6>  seventh field type
   * @param <T7>  eighth field type
   * @param <T8>  ninth field type
   * @param <T9>  tenth field type
   * @param <T10> eleventh field type
   * @param <T11> twelfth field type
   * @param <T12> thirteenth field type
   * @param <T13> fourteenth field type
   * @param <T14> fifteenth field type
   * @param <T15> sixteenth field type
   * @param <T16> seventeenth field type
   * @param m0    first field mapper
   * @param m1    second field mapper
   * @param m2    third field mapper
   * @param m3    fourth field mapper
   * @param m4    fifth field mapper
   * @param m5    sixth field mapper
   * @param m6    seventh field mapper
   * @param m7    eighth field mapper
   * @param m8    ninth field mapper
   * @param m9    tenth field mapper
   * @param m10   eleventh field mapper
   * @param m11   twelfth field mapper
   * @param m12   thirteenth field mapper
   * @param m13   fourteenth field mapper
   * @param m14   fifteenth field mapper
   * @param m15   sixteenth field mapper
   * @param m16   seventeenth field mapper
   * @return septendecuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Function<T, Septendecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16) {
    return new SeptendecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16);
  }

  /**
   * Creates eighteen-field tuple mapper.
   * 
   * @param <T>   source type
   * @param <T0>  first field type
   * @param <T1>  second field type
   * @param <T2>  third field type
   * @param <T3>  fourth field type
   * @param <T4>  fifth field type
   * @param <T5>  sixth field type
   * @param <T6>  seventh field type
   * @param <T7>  eighth field type
   * @param <T8>  ninth field type
   * @param <T9>  tenth field type
   * @param <T10> eleventh field type
   * @param <T11> twelfth field type
   * @param <T12> thirteenth field type
   * @param <T13> fourteenth field type
   * @param <T14> fifteenth field type
   * @param <T15> sixteenth field type
   * @param <T16> seventeenth field type
   * @param <T17> eighteenth field type
   * @param m0    first field mapper
   * @param m1    second field mapper
   * @param m2    third field mapper
   * @param m3    fourth field mapper
   * @param m4    fifth field mapper
   * @param m5    sixth field mapper
   * @param m6    seventh field mapper
   * @param m7    eighth field mapper
   * @param m8    ninth field mapper
   * @param m9    tenth field mapper
   * @param m10   eleventh field mapper
   * @param m11   twelfth field mapper
   * @param m12   thirteenth field mapper
   * @param m13   fourteenth field mapper
   * @param m14   fifteenth field mapper
   * @param m15   sixteenth field mapper
   * @param m16   seventeenth field mapper
   * @param m17   eighteenth field mapper
   * @return octodecuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> Function<T, Octodecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17) {
    return new OctodecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17);
  }

  /**
   * Creates nineteen-field tuple mapper.
   * 
   * @param <T>   source type
   * @param <T0>  first field type
   * @param <T1>  second field type
   * @param <T2>  third field type
   * @param <T3>  fourth field type
   * @param <T4>  fifth field type
   * @param <T5>  sixth field type
   * @param <T6>  seventh field type
   * @param <T7>  eighth field type
   * @param <T8>  ninth field type
   * @param <T9>  tenth field type
   * @param <T10> eleventh field type
   * @param <T11> twelfth field type
   * @param <T12> thirteenth field type
   * @param <T13> fourteenth field type
   * @param <T14> fifteenth field type
   * @param <T15> sixteenth field type
   * @param <T16> seventeenth field type
   * @param <T17> eighteenth field type
   * @param <T18> nineteenth field type
   * @param m0    first field mapper
   * @param m1    second field mapper
   * @param m2    third field mapper
   * @param m3    fourth field mapper
   * @param m4    fifth field mapper
   * @param m5    sixth field mapper
   * @param m6    seventh field mapper
   * @param m7    eighth field mapper
   * @param m8    ninth field mapper
   * @param m9    tenth field mapper
   * @param m10   eleventh field mapper
   * @param m11   twelfth field mapper
   * @param m12   thirteenth field mapper
   * @param m13   fourteenth field mapper
   * @param m14   fifteenth field mapper
   * @param m15   sixteenth field mapper
   * @param m16   seventeenth field mapper
   * @param m17   eighteenth field mapper
   * @param m18   nineteenth field mapper
   * @return novemdecuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> Function<T, Novemdecuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17, Function<T, T18> m18) {
    return new NovemdecupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17,
        m18);
  }

  /**
   * Creates twenty-field tuple mapper.
   * 
   * @param <T>   source type
   * @param <T0>  first field type
   * @param <T1>  second field type
   * @param <T2>  third field type
   * @param <T3>  fourth field type
   * @param <T4>  fifth field type
   * @param <T5>  sixth field type
   * @param <T6>  seventh field type
   * @param <T7>  eighth field type
   * @param <T8>  ninth field type
   * @param <T9>  tenth field type
   * @param <T10> eleventh field type
   * @param <T11> twelfth field type
   * @param <T12> thirteenth field type
   * @param <T13> fourteenth field type
   * @param <T14> fifteenth field type
   * @param <T15> sixteenth field type
   * @param <T16> seventeenth field type
   * @param <T17> eighteenth field type
   * @param <T18> nineteenth field type
   * @param <T19> twentieth field type
   * @param m0    first field mapper
   * @param m1    second field mapper
   * @param m2    third field mapper
   * @param m3    fourth field mapper
   * @param m4    fifth field mapper
   * @param m5    sixth field mapper
   * @param m6    seventh field mapper
   * @param m7    eighth field mapper
   * @param m8    ninth field mapper
   * @param m9    tenth field mapper
   * @param m10   eleventh field mapper
   * @param m11   twelfth field mapper
   * @param m12   thirteenth field mapper
   * @param m13   fourteenth field mapper
   * @param m14   fifteenth field mapper
   * @param m15   sixteenth field mapper
   * @param m16   seventeenth field mapper
   * @param m17   eighteenth field mapper
   * @param m18   nineteenth field mapper
   * @param m19   twentieth field mapper
   * @return vigintuple tuple mapper
   */
  public static <T, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> Function<T, Vigintuple<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>> of(
      Function<T, T0> m0, Function<T, T1> m1, Function<T, T2> m2, Function<T, T3> m3, Function<T, T4> m4,
      Function<T, T5> m5, Function<T, T6> m6, Function<T, T7> m7, Function<T, T8> m8, Function<T, T9> m9,
      Function<T, T10> m10, Function<T, T11> m11, Function<T, T12> m12, Function<T, T13> m13, Function<T, T14> m14,
      Function<T, T15> m15, Function<T, T16> m16, Function<T, T17> m17, Function<T, T18> m18, Function<T, T19> m19) {
    return new VigintupleMapperImpl<>(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17,
        m18, m19);
  }

}