package com.redis.om.spring.search.stream.predicates.numeric;

import java.lang.reflect.Field;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.Values;

public class GreaterThanPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;

  public GreaterThanPredicate(Field field, T value) {
    super(field);
    this.value = value;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.GREATER_THAN;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    Class<?> cls = value.getClass();
    if (cls == Integer.class) {
      return QueryBuilder.intersect(root).add(getField().getName(), Values.gt(Integer.valueOf(value.toString())));
    } else {
      return QueryBuilder.intersect(root).add(getField().getName(), Values.gt(Double.valueOf(value.toString())));
    }
  }
}
