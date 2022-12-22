package com.redis.om.spring.search.stream.predicates.geo;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.GeoValue;

public class NearPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final Point point;
  private final Distance distance;

  public NearPredicate(SearchFieldAccessor field, Point point, Distance distance) {
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

    return QueryBuilders.intersect(root).add(getSearchAlias(), geoValue);
  }

}
