package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.*;

/**
 * Represents a tuple of eleven elements.
 * <p>
 * This interface extends {@link Tuple} and provides access to eleven typed elements
 * through getter methods and accessor functions. An undecuple (11-element tuple) is useful
 * for operations that need to return or process exactly eleven related values together,
 * such as complex aggregation results, multi-field database projections, or composite
 * data structures in Redis OM Spring applications.
 * </p>
 * <p>
 * In the context of Redis OM Spring, undecuple tuples are commonly used for:
 * <ul>
 * <li>Complex aggregation results from RediSearch queries that group by eleven fields</li>
 * <li>Multi-field projections from Redis documents or hashes containing eleven attributes</li>
 * <li>Batch operations returning eleven related values</li>
 * <li>Entity streams that transform data into eleven-component results</li>
 * <li>Complex composite keys or identifiers with eleven parts</li>
 * <li>Result sets from queries joining multiple data sources with eleven total fields</li>
 * </ul>
 *
 * @param <E1>  the type of the first element
 * @param <E2>  the type of the second element
 * @param <E3>  the type of the third element
 * @param <E4>  the type of the fourth element
 * @param <E5>  the type of the fifth element
 * @param <E6>  the type of the sixth element
 * @param <E7>  the type of the seventh element
 * @param <E8>  the type of the eighth element
 * @param <E9>  the type of the ninth element
 * @param <E10> the type of the tenth element
 * @param <E11> the type of the eleventh element
 */
public interface Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> extends Tuple {

  /**
   * Returns a function that extracts the first element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the first element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> FirstAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E1> getFirstGetter() {
    return Undecuple::getFirst;
  }

  /**
   * Returns a function that extracts the second element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the second element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> SecondAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E2> getSecondGetter() {
    return Undecuple::getSecond;
  }

  /**
   * Returns a function that extracts the third element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the third element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> ThirdAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E3> getThirdGetter() {
    return Undecuple::getThird;
  }

  /**
   * Returns a function that extracts the fourth element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the fourth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> FourthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E4> getFourthGetter() {
    return Undecuple::getFourth;
  }

  /**
   * Returns a function that extracts the fifth element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the fifth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> FifthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E5> getFifthGetter() {
    return Undecuple::getFifth;
  }

  /**
   * Returns a function that extracts the sixth element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the sixth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> SixthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E6> getSixthGetter() {
    return Undecuple::getSixth;
  }

  /**
   * Returns a function that extracts the seventh element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the seventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> SeventhAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E7> getSeventhGetter() {
    return Undecuple::getSeventh;
  }

  /**
   * Returns a function that extracts the eighth element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the eighth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> EighthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E8> getEighthGetter() {
    return Undecuple::getEighth;
  }

  /**
   * Returns a function that extracts the ninth element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the ninth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> NinthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E9> getNinthGetter() {
    return Undecuple::getNinth;
  }

  /**
   * Returns a function that extracts the tenth element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the tenth element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> TenthAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E10> getTenthGetter() {
    return Undecuple::getTenth;
  }

  /**
   * Returns a function that extracts the eleventh element from an Undecuple.
   *
   * @param <E1>  the type of the first element
   * @param <E2>  the type of the second element
   * @param <E3>  the type of the third element
   * @param <E4>  the type of the fourth element
   * @param <E5>  the type of the fifth element
   * @param <E6>  the type of the sixth element
   * @param <E7>  the type of the seventh element
   * @param <E8>  the type of the eighth element
   * @param <E9>  the type of the ninth element
   * @param <E10> the type of the tenth element
   * @param <E11> the type of the eleventh element
   * @return a function that extracts the eleventh element
   */
  static <E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11> EleventhAccessor<Undecuple<E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11>, E11> getEleventhGetter() {
    return Undecuple::getEleventh;
  }

  /**
   * Returns the first element of this tuple.
   *
   * @return the first element
   */
  E1 getFirst();

  /**
   * Returns the second element of this tuple.
   *
   * @return the second element
   */
  E2 getSecond();

  /**
   * Returns the third element of this tuple.
   *
   * @return the third element
   */
  E3 getThird();

  /**
   * Returns the fourth element of this tuple.
   *
   * @return the fourth element
   */
  E4 getFourth();

  /**
   * Returns the fifth element of this tuple.
   *
   * @return the fifth element
   */
  E5 getFifth();

  /**
   * Returns the sixth element of this tuple.
   *
   * @return the sixth element
   */
  E6 getSixth();

  /**
   * Returns the seventh element of this tuple.
   *
   * @return the seventh element
   */
  E7 getSeventh();

  /**
   * Returns the eighth element of this tuple.
   *
   * @return the eighth element
   */
  E8 getEighth();

  /**
   * Returns the ninth element of this tuple.
   *
   * @return the ninth element
   */
  E9 getNinth();

  /**
   * Returns the tenth element of this tuple.
   *
   * @return the tenth element
   */
  E10 getTenth();

  /**
   * Returns the eleventh element of this tuple.
   *
   * @return the eleventh element
   */
  E11 getEleventh();

  @Override
  default int size() {
    return 11;
  }

  /**
   * Returns the element at the specified index.
   *
   * @param index the index of the element to return (0-based)
   * @return the element at the specified index
   * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size())
   */
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
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}