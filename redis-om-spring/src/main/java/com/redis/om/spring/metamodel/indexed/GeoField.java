package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.geo.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.geo.NearPredicate;
import com.redis.om.spring.search.stream.predicates.geo.NotEqualPredicate;
import com.redis.om.spring.search.stream.predicates.geo.OutsideOfPredicate;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

public class GeoField<E, T> extends MetamodelField<E, T> {

  public GeoField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  public GeoField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  public EqualPredicate<E, T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor, value);
  }

  public EqualPredicate<E, T> eq(String xy) {
    return new EqualPredicate<>(searchFieldAccessor, xy);
  }

  public EqualPredicate<E, T> eq(double x, double y) {
    return new EqualPredicate<>(searchFieldAccessor, x, y);
  }

  public NotEqualPredicate<E, T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }

  public NotEqualPredicate<E, T> notEq(String xy) {
    return new NotEqualPredicate<>(searchFieldAccessor, xy);
  }

  public NotEqualPredicate<E, T> notEq(double x, double y) {
    return new NotEqualPredicate<>(searchFieldAccessor, x, y);
  }

  public NearPredicate<E, T> near(Point point, Distance distance) {
    return new NearPredicate<>(searchFieldAccessor, point, distance);
  }

  public OutsideOfPredicate<E, T> outsideOf(Point point, Distance distance) {
    return new OutsideOfPredicate<>(searchFieldAccessor, point, distance);
  }

}
