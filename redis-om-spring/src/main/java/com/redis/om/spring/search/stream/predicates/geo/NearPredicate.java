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
 * A geospatial proximity predicate that filters entities based on their distance
 * from a specified geographic point.
 * 
 * <p>This predicate is designed for use with fields annotated with {@code @GeoIndexed}
 * and performs geospatial searches to find entities within a certain radius of a
 * reference point. It supports various distance units (kilometers, miles, meters, etc.).</p>
 * 
 * <p>The predicate generates Redis geospatial queries that efficiently search within
 * circular geographic areas using Redis's built-in geospatial indexing capabilities.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find stores within 5 km of a location
 * Point location = new Point(-122.4194, 37.7749); // San Francisco
 * Distance radius = new Distance(5, Metrics.KILOMETERS);
 * entityStream.filter(Store$.LOCATION.near(location, radius))
 * 
 * // Find restaurants within 1 mile
 * entityStream.filter(Restaurant$.COORDINATES.near(userLocation,
 * new Distance(1, Metrics.MILES)))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (typically Point)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see org.springframework.data.geo.Point
 * @see org.springframework.data.geo.Distance
 * @see OutsideOfPredicate
 */
public class NearPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  /** The center point for the proximity search */
  private final Point point;

  /** The maximum distance from the center point */
  private final Distance distance;

  /**
   * Creates a new NearPredicate for the specified field, center point, and radius.
   * 
   * @param field    the field accessor for the target geospatial field
   * @param point    the center point for the proximity search
   * @param distance the maximum distance from the center point
   */
  public NearPredicate(SearchFieldAccessor field, Point point, Distance distance) {
    super(field);
    this.point = point;
    this.distance = distance;
  }

  /**
   * Returns the center point for the proximity search.
   * 
   * @return the geographic center point
   */
  public Point getPoint() {
    return point;
  }

  /**
   * Returns the maximum distance from the center point.
   * 
   * @return the search radius as a Distance object
   */
  public Distance getDistance() {
    return distance;
  }

  /**
   * Applies this geospatial proximity predicate to the given query node.
   * 
   * <p>This method generates a Redis geospatial query that finds entities within
   * the specified distance from the center point. The query uses Redis's built-in
   * geospatial indexing for efficient circular area searches.</p>
   * 
   * <p>If either the point or distance is null/empty, the predicate is ignored
   * and the original root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the geospatial condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    boolean paramsPresent = isNotEmpty(point) && isNotEmpty(distance);
    if (paramsPresent) {
      GeoValue geoValue = new GeoValue(getPoint().getX(), getPoint().getY(), getDistance().getValue(), ObjectUtils
          .getDistanceUnit(getDistance()));
      return QueryBuilders.intersect(root).add(getSearchAlias(), geoValue);
    } else
      return root;
  }

}
