package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.search.stream.predicates.NearPredicate;

public class GeoFieldOperationInterceptor<E, T> extends FieldOperationInterceptor<E, T> {

  public GeoFieldOperationInterceptor(Field field, boolean indexed) {
    super(field, indexed);
  }
  
  public NearPredicate<? super E,T> near(Point point, Distance distance) {
    return new NearPredicate<E,T>(field,point,distance);
  }

}
