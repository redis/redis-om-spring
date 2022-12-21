package com.redis.om.spring.search.stream.predicates.geo;

import java.lang.reflect.Field;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import org.springframework.data.geo.Point;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;

public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;
  private Double x;
  private Double y;

  public EqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
    if (value.getClass() == Point.class) {
      Point point = (Point)value;
      x = point.getX();
      y = point.getY();
    }
  }
  
  public EqualPredicate(SearchFieldAccessor field, String xy) {
    super(field);
    String[] coordinates = xy.split(",");
    x = Double.parseDouble(coordinates[0]);
    y = Double.parseDouble(coordinates[1]);
  }
  
  public EqualPredicate(SearchFieldAccessor field, Double x, Double y) {
    super(field);
    this.x = x;
    this.y = y;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {     
    return QueryBuilder.intersect(root).add(getSearchAlias(), String.format("[%s %s 0.0001 mi]", x, y));
  }

}
