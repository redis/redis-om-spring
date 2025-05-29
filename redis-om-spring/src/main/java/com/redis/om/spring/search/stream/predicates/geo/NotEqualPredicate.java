package com.redis.om.spring.search.stream.predicates.geo;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.geo.Point;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

/**
 * Predicate for performing "not equal" operations on geo-indexed fields.
 * This predicate excludes documents where the specified geo field is within
 * a very small radius (0.0005 mi) of the given point, effectively excluding
 * points that are practically at the same location.
 *
 * @param <E> the entity type being queried
 * @param <T> the type of the value being compared (typically Point or coordinates)
 */
public class NotEqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;
  private Double x;
  private Double y;

  /**
   * Constructs a NotEqualPredicate for the specified field and Point value.
   *
   * @param field the search field accessor for the geo field to be queried
   * @param value the Point value that should not equal the field value
   */
  public NotEqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
    if (value.getClass() == Point.class) {
      Point point = (Point) value;
      x = point.getX();
      y = point.getY();
    }
  }

  /**
   * Constructs a NotEqualPredicate for the specified field and coordinate string.
   *
   * @param field the search field accessor for the geo field to be queried
   * @param xy    the coordinate string in "x,y" format that should not equal the field value
   */
  public NotEqualPredicate(SearchFieldAccessor field, String xy) {
    super(field);
    String[] coordinates = xy.split(",");
    x = Double.parseDouble(coordinates[0]);
    y = Double.parseDouble(coordinates[1]);
  }

  /**
   * Constructs a NotEqualPredicate for the specified field and individual coordinates.
   *
   * @param field the search field accessor for the geo field to be queried
   * @param x     the longitude coordinate that should not equal the field value
   * @param y     the latitude coordinate that should not equal the field value
   */
  public NotEqualPredicate(SearchFieldAccessor field, Double x, Double y) {
    super(field);
    this.x = x;
    this.y = y;
  }

  /**
   * Gets the value that should not equal the field value.
   *
   * @return the Point value to exclude from matches
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies the "not equal" predicate to the query node tree.
   * Creates a disjunct (negated) query that excludes documents where the geo field
   * is within a very small radius (0.0005 mi) of the specified coordinates.
   *
   * @param root the root query node to which this predicate will be applied
   * @return the modified query node with the geo "not equal" condition applied,
   *         or the original root if coordinates are not present
   */
  @Override
  public Node apply(Node root) {
    boolean paramsPresent = ObjectUtils.isNotEmpty(x) && ObjectUtils.isNotEmpty(y);
    //TODO: default and default distance metric should be obtained from RedisOMProperties
    return paramsPresent ?
        QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.value(String.format(
            "[%s %s 0.0005 mi]", x, y)))) :
        root;
  }

}
