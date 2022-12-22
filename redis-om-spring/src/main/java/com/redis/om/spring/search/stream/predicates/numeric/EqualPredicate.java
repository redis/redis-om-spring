package com.redis.om.spring.search.stream.predicates.numeric;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;

  public EqualPredicate(Field field, T value) {
    super(field);
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    Class<?> cls = getValue().getClass();
    if (cls == LocalDate.class) {
      LocalDate localDate = (LocalDate) getValue();
      Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
      Long unixTime = instant.getEpochSecond();
      return QueryBuilders.intersect(root).add(getField().getName(), Values.eq(unixTime));
    } else if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getField().getName(), Values.eq(Integer.valueOf(getValue().toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(getField().getName(), Values.eq(Double.valueOf(getValue().toString())));
    } else {
      return root;
    }
  }

}
