package com.redis.om.spring.search.stream.predicates.fulltext;

import java.lang.reflect.Field;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

public class NotLikePredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private T value;

  public NotLikePredicate(Field field, T value) {
    super(field);
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    return QueryBuilders.intersect(root)
        .add(QueryBuilders.disjunct(getField().getName(), Values.value("%%%" + getValue().toString() + "%%%")));
  }

}
