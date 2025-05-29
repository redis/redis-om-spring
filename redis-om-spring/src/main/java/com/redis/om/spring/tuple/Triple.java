package com.redis.om.spring.tuple;

import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;

/**
 * Represents a tuple of three elements.
 * <p>
 * This interface extends {@link Tuple} and provides access to three typed elements
 * through getter methods and accessor functions. A triple (3-element tuple) is useful
 * for operations that need to return or process exactly three related values together,
 * such as coordinates (x, y, z), RGB color values, or composite keys in Redis OM Spring
 * applications.
 * </p>
 * <p>
 * In the context of Redis OM Spring, triple tuples are commonly used for:
 * <ul>
 * <li>Aggregation results from RediSearch queries that group by three fields</li>
 * <li>Three-field projections from Redis documents or hashes</li>
 * <li>Composite keys with three components</li>
 * <li>Entity streams that transform data into three-component results</li>
 * <li>Geospatial operations that include coordinates and additional metadata</li>
 * </ul>
 *
 * @param <E1> the type of the first element
 * @param <E2> the type of the second element
 * @param <E3> the type of the third element
 */
public interface Triple<E1, E2, E3> extends Tuple {

  /**
   * Returns a function that extracts the first element from a Triple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @return a function that extracts the first element
   */
  static <E1, E2, E3> FirstAccessor<Triple<E1, E2, E3>, E1> getFirstGetter() {
    return Triple::getFirst;
  }

  /**
   * Returns a function that extracts the second element from a Triple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @return a function that extracts the second element
   */
  static <E1, E2, E3> SecondAccessor<Triple<E1, E2, E3>, E2> getSecondGetter() {
    return Triple::getSecond;
  }

  /**
   * Returns a function that extracts the third element from a Triple.
   *
   * @param <E1> the type of the first element
   * @param <E2> the type of the second element
   * @param <E3> the type of the third element
   * @return a function that extracts the third element
   */
  static <E1, E2, E3> ThirdAccessor<Triple<E1, E2, E3>, E3> getThirdGetter() {
    return Triple::getThird;
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

  @Override
  default int size() {
    return 3;
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
      default -> throw new IndexOutOfBoundsException(String.format("Index %d is outside bounds of tuple of degree %s",
          index, size()));
    };
  }
}