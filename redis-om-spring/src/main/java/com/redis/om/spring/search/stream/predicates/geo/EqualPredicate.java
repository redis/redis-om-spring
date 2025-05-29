package com.redis.om.spring.search.stream.predicates.geo;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.geo.Point;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * Predicate for geo field equality searches.
 * 
 * @param <E> entity type
 * @param <T> value type
 */
public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;
  private Double x;
  private Double y;

  /**
   * Constructor with generic value.
   *
   * @param field the search field accessor
   * @param value the value to match
   */
  public EqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
    if (value.getClass() == Point.class) {
      Point point = (Point) value;
      x = point.getX();
      y = point.getY();
    }
  }

  /**
   * Constructor with coordinate string.
   *
   * @param field the search field accessor
   * @param xy    the coordinates as a comma-separated string (e.g., "x,y")
   */
  public EqualPredicate(SearchFieldAccessor field, String xy) {
    super(field);
    String[] coordinates = xy.split(",");
    x = Double.parseDouble(coordinates[0]);
    y = Double.parseDouble(coordinates[1]);
  }

  /**
   * Constructor with x,y coordinates.
   *
   * @param field the search field accessor
   * @param x     the x coordinate
   * @param y     the y coordinate
   */
  public EqualPredicate(SearchFieldAccessor field, Double x, Double y) {
    super(field);
    this.x = x;
    this.y = y;
  }

  /**
   * Returns the predicate value.
   *
   * @return the value being matched by this predicate
   */
  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    boolean paramsPresent = ObjectUtils.isNotEmpty(x) && ObjectUtils.isNotEmpty(y);
    return paramsPresent ?
        QueryBuilders.intersect(root).add(getSearchAlias(), String.format("[%s %s 0.0005 mi]", x, y)) :
        root;
  }

}
