package com.redis.om.spring.search.stream.predicates.geo;

import java.lang.reflect.Field;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.querybuilder.GeoValue;
import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;

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

  public Point getPoint() {
    return point;
  }

  public Distance getDistance() {
    return distance;
  }

  @Override
  public Node apply(Node root) {
    GeoValue geoValue = new GeoValue(point.getX(), point.getY(), distance.getValue(),
        ObjectUtils.getDistanceUnit(distance));

    return QueryBuilder.intersect(root).add(getField().getName(), geoValue);
  }

}
