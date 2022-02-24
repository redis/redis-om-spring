package com.redis.om.spring.search.stream.predicates.tag;

import java.lang.reflect.Field;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.QueryNode;

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
    if (Iterable.class.isAssignableFrom(value.getClass())) {
      Iterable<?> values = (Iterable<?>) value;
      QueryNode and = QueryBuilder.intersect();
      for (Object v : values) {
        and.add(getField().getName(), "{" + v.toString() + "}");
      }
      return QueryBuilder.intersect(root, and);
    } else {
      return QueryBuilder.intersect(root).add(getField().getName(), "{" + value.toString() + "}");
    }
  }

}
