package com.redis.om.spring.search.stream.predicates.geo;

import java.lang.reflect.Field;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;

public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;

  public EqualPredicate(Field field, T value) {
    super(field);
    this.value = value;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.EQUAL;
  }

  public T getValue() {
    return value;
  }
  
  @Override
  public Node apply(Node root) {
    return QueryBuilder.intersect(root).add(getField().getName(), value.toString());
  }

}
