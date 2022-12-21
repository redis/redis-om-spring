package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.search.stream.predicates.geo.NotEqualPredicate;
import com.redis.om.spring.search.stream.predicates.geo.OutsideOfPredicate;
import com.redis.om.spring.search.stream.predicates.geo.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.geo.NearPredicate;

public class GeoField<E, T> extends MetamodelField<E, T> {

  public GeoField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }
  
  public EqualPredicate<? super E,T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor,value);
  }
  
  public EqualPredicate<? super E,T> eq(String xy) {
    return new EqualPredicate<>(searchFieldAccessor,xy);
  }
  
  public EqualPredicate<? super E,T> eq(double x, double y) {
    return new EqualPredicate<>(searchFieldAccessor, x, y);
  }
  
  public NotEqualPredicate<E,T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor,value);
  }
  
  public NotEqualPredicate<? super E,T> notEq(String xy) {
    return new NotEqualPredicate<>(searchFieldAccessor,xy);
  }
  
  public NotEqualPredicate<? super E,T> notEq(double x, double y) {
    return new NotEqualPredicate<>(searchFieldAccessor, x, y);
  }
  
  public NearPredicate<? super E,T> near(Point point, Distance distance) {
    return new NearPredicate<>(searchFieldAccessor,point,distance);
  }
  
  public OutsideOfPredicate<? super E,T> outsideOf(Point point, Distance distance) {
    return new OutsideOfPredicate<>(searchFieldAccessor,point,distance);
  }

}
