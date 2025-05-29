package com.redis.om.spring.metamodel.indexed;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.geo.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.geo.NearPredicate;
import com.redis.om.spring.search.stream.predicates.geo.NotEqualPredicate;
import com.redis.om.spring.search.stream.predicates.geo.OutsideOfPredicate;

/**
 * Metamodel field for geographic (geo) fields. Provides methods to create
 * geospatial predicates for equality, proximity, and distance-based searches.
 *
 * @param <E> the entity type
 * @param <T> the field value type
 */
public class GeoField<E, T> extends MetamodelField<E, T> {

  /**
   * Constructs a new GeoField with a search field accessor.
   *
   * @param field   the search field accessor
   * @param indexed whether the field is indexed
   */
  public GeoField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Constructs a new GeoField with a target class and field name.
   *
   * @param targetClass the entity class
   * @param fieldName   the name of the field
   */
  public GeoField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  /**
   * Creates an equality predicate for the given value.
   *
   * @param value the value to match
   * @return an EqualPredicate for the value
   */
  public EqualPredicate<E, T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates an equality predicate for the given coordinate string.
   *
   * @param xy the coordinates as a comma-separated string (e.g., "x,y")
   * @return an EqualPredicate for the coordinates
   */
  public EqualPredicate<E, T> eq(String xy) {
    return new EqualPredicate<>(searchFieldAccessor, xy);
  }

  /**
   * Creates an equality predicate for the given x,y coordinates.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   * @return an EqualPredicate for the coordinates
   */
  public EqualPredicate<E, T> eq(double x, double y) {
    return new EqualPredicate<>(searchFieldAccessor, x, y);
  }

  /**
   * Creates a not-equal predicate for the given value.
   *
   * @param value the value to exclude
   * @return a NotEqualPredicate for the value
   */
  public NotEqualPredicate<E, T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a not-equal predicate for the given coordinate string.
   *
   * @param xy the coordinates as a comma-separated string (e.g., "x,y")
   * @return a NotEqualPredicate for the coordinates
   */
  public NotEqualPredicate<E, T> notEq(String xy) {
    return new NotEqualPredicate<>(searchFieldAccessor, xy);
  }

  /**
   * Creates a not-equal predicate for the given x,y coordinates.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   * @return a NotEqualPredicate for the coordinates
   */
  public NotEqualPredicate<E, T> notEq(double x, double y) {
    return new NotEqualPredicate<>(searchFieldAccessor, x, y);
  }

  /**
   * Creates a proximity predicate that matches locations within the specified distance from a point.
   *
   * @param point    the center point for the proximity search
   * @param distance the maximum distance from the point
   * @return a NearPredicate for the proximity search
   */
  public NearPredicate<E, T> near(Point point, Distance distance) {
    return new NearPredicate<>(searchFieldAccessor, point, distance);
  }

  /**
   * Creates a predicate that matches locations outside the specified distance from a point.
   *
   * @param point    the center point for the exclusion search
   * @param distance the minimum distance from the point
   * @return an OutsideOfPredicate for the exclusion search
   */
  public OutsideOfPredicate<E, T> outsideOf(Point point, Distance distance) {
    return new OutsideOfPredicate<>(searchFieldAccessor, point, distance);
  }

}
