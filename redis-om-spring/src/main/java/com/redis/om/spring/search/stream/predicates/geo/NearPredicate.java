package com.redis.om.spring.search.stream.predicates.geo;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import redis.clients.jedis.search.querybuilder.GeoValue;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

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
    boolean paramsPresent = isNotEmpty(point) && isNotEmpty(distance);
    if (paramsPresent) {
      GeoValue geoValue = new GeoValue(getPoint().getX(), getPoint().getY(), getDistance().getValue(), ObjectUtils.getDistanceUnit(getDistance()));
      return QueryBuilders.intersect(root).add(getSearchAlias(), geoValue);
    } else return root;
  }

}
