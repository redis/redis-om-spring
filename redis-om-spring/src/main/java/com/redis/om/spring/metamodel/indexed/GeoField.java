package com.redis.om.spring.metamodel.indexed;

import java.lang.reflect.Field;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.search.stream.predicates.geo.NotEqualPredicate;
import com.redis.om.spring.search.stream.predicates.geo.OutsideOfPredicate;
import com.redis.om.spring.search.stream.predicates.geo.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.geo.NearPredicate;

public class GeoField<E, T> extends MetamodelField<E, T> {

  public GeoField(Field field, boolean indexed) {
    super(field, indexed);
  }
  
  public EqualPredicate<? super E,T> eq(T value) {
    return new EqualPredicate<>(field,value);
  }
  
  public EqualPredicate<? super E,T> eq(String xy) {
    return new EqualPredicate<>(field,xy);
  }
  
  public EqualPredicate<? super E,T> eq(double x, double y) {
    return new EqualPredicate<>(field, x, y);
  }
  
  public NotEqualPredicate<E,T> notEq(T value) {
    return new NotEqualPredicate<>(field,value);
  }
  
  public NotEqualPredicate<? super E,T> notEq(String xy) {
    return new NotEqualPredicate<>(field,xy);
  }
  
  public NotEqualPredicate<? super E,T> notEq(double x, double y) {
    return new NotEqualPredicate<>(field, x, y);
  }
  
  public NearPredicate<? super E,T> near(Point point, Distance distance) {
    return new NearPredicate<>(field,point,distance);
  }
  
  public OutsideOfPredicate<? super E,T> outsideOf(Point point, Distance distance) {
    return new OutsideOfPredicate<>(field,point,distance);
  }

}
