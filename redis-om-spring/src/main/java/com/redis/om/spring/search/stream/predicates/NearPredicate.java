package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

public class NearPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private Point point;
  private Distance distance;

  public NearPredicate(Field field, Point point, Distance distance) {
    super(field);
    this.point = point;
    this.distance = distance;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.NEAR;
  }

  @Override
  public boolean test(T t) {
    // TODO Auto-generated method stub
    return false;
  }

  public Point getPoint() {
    return point;
  }

  public Distance getDistance() {
    return distance;
  }

}
