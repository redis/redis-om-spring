package com.redis.om.spring.metamodel.indexed;

import java.lang.reflect.Field;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.search.stream.predicates.geo.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.geo.NearPredicate;

public class GeoField<E, T> extends MetamodelField<E, T> {

  public GeoField(Field field, boolean indexed) {
    super(field, indexed);
  }
  
  public EqualPredicate<? super E,T> eq(T value) {
    return new EqualPredicate<E,T>(field,value);
  }
  
  public NearPredicate<? super E,T> near(Point point, Distance distance) {
    return new NearPredicate<E,T>(field,point,distance);
  }

}
