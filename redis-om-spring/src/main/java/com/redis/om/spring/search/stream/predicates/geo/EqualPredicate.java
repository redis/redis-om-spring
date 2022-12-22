package com.redis.om.spring.search.stream.predicates.geo;

import java.lang.reflect.Field;

import org.springframework.data.geo.Point;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;
  private Double x;
  private Double y;

  public EqualPredicate(Field field, T value) {
    super(field);
    this.value = value;
    if (value.getClass() == Point.class) {
      Point point = (Point)value;
      x = point.getX();
      y = point.getY();
    }
  }
  
  public EqualPredicate(Field field, String xy) {
    super(field);
    String[] coordinates = xy.split(",");
    x = Double.parseDouble(coordinates[0]);
    y = Double.parseDouble(coordinates[1]);
  }
  
  public EqualPredicate(Field field, Double x, Double y) {
    super(field);
    this.x = x;
    this.y = y;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {     
    return QueryBuilders.intersect(root).add(getField().getName(), String.format("[%s %s 0.0001 mi]", x, y));
  }

}
