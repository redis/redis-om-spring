package com.redis.om.spring.search.stream.predicates.geo;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.querybuilder.GeoValue;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * Geospatial predicate for finding entities outside a specified circular area.
 * <p>
 * This predicate implements a geospatial query that finds entities whose indexed
 * geographic coordinates fall outside a circular area defined by a center point
 * and radius distance. It's the inverse of the "within" geospatial operation and
 * is part of the Redis OM Spring Entity Streams API for spatial queries.
 * </p>
 * <p>
 * The predicate translates to a RediSearch disjunct operation, which excludes
 * results that would match the circular area defined by the point and distance.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * Point center = new Point(-122.4194, 37.7749); // San Francisco
 * Distance radius = new Distance(10, Metrics.KILOMETERS);
 * 
 * EntityStream.of(Store.class)
 * .filter(Store$.LOCATION.outsideOf(center, radius))
 * .collect(Collectors.toList());
 * </pre>
 *
 * @param <E> the entity type being queried
 * @param <T> the field type being compared (typically Point or GeoLocation)
 * @see BaseAbstractPredicate
 * @see org.springframework.data.geo.Point
 * @see org.springframework.data.geo.Distance
 * @since 0.1.0
 */
public class OutsideOfPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final Point point;
  private final Distance distance;

  /**
   * Constructs a new OutsideOfPredicate with the specified field, center point, and radius.
   * <p>
   * Creates a geospatial predicate that will match entities whose indexed geographic
   * field values fall outside the circular area defined by the center point and distance.
   * </p>
   *
   * @param field    the search field accessor for the geographic field being queried
   * @param point    the center point of the circular area to exclude
   * @param distance the radius distance from the center point
   */
  public OutsideOfPredicate(SearchFieldAccessor field, Point point, Distance distance) {
    super(field);
    this.point = point;
    this.distance = distance;
  }

  /**
   * Returns the center point of the circular area to exclude.
   *
   * @return the center Point of the exclusion area
   */
  public Point getPoint() {
    return point;
  }

  /**
   * Returns the radius distance from the center point.
   *
   * @return the Distance representing the radius of the exclusion area
   */
  public Distance getDistance() {
    return distance;
  }

  /**
   * Applies this outside-of predicate to create a RediSearch query node.
   * <p>
   * This method creates a RediSearch disjunct operation that excludes entities
   * within the specified circular area. The disjunct operation is the inverse
   * of an intersection, effectively finding entities outside the defined area.
   * </p>
   * <p>
   * If either the point or distance parameters are null or empty, the method
   * returns the root node unchanged, effectively making this predicate a no-op.
   * </p>
   *
   * @param root the root query node to build upon
   * @return a Node representing the geospatial exclusion query, or the original
   *         root node if parameters are invalid
   */
  @Override
  public Node apply(Node root) {
    boolean paramsPresent = isNotEmpty(point) && isNotEmpty(distance);
    if (paramsPresent) {
      GeoValue geoValue = new GeoValue(getPoint().getX(), getPoint().getY(), getDistance().getValue(), ObjectUtils
          .getDistanceUnit(getDistance()));

      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), geoValue));
    } else
      return root;
  }

}
