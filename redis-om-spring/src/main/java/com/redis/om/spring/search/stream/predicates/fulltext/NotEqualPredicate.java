package com.redis.om.spring.search.stream.predicates.fulltext;

import java.lang.reflect.Field;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

public class NotEqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;

  public NotEqualPredicate(Field field, T value) {
    super(field);
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    Object value = getValue();
    String fieldName = getField().getName();
    return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(fieldName, Values.value(value.toString())));
  }

}
