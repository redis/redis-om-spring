package com.redis.om.spring.search.stream.predicates.geo;

import java.lang.reflect.Field;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.querybuilder.GeoValue;
import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;

public class OutsideOfPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private Point point;
  private Distance distance;

  public OutsideOfPredicate(SearchFieldAccessor field, Point point, Distance distance) {
    super(field);
    this.point = point;
    this.distance = distance;
  }

  public Point getPoint() {
    return point;
  }

  public Distance getDistance() {
    return distance;
  }

  @Override
  public Node apply(Node root) {
    GeoValue geoValue = new GeoValue(getPoint().getX(), getPoint().getY(), getDistance().getValue(),
        ObjectUtils.getDistanceUnit(getDistance()));

    return QueryBuilder.intersect(root)
        .add(QueryBuilder.disjunct(getSearchAlias(), geoValue));
  }

}
