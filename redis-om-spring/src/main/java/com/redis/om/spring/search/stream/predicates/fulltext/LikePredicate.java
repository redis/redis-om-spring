package com.redis.om.spring.search.stream.predicates.fulltext;

import java.lang.reflect.Field;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;

public class LikePredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private T value;

  public LikePredicate(Field field, T value) {
    super(field);
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    return QueryBuilder.intersect(root).add(getField().getName(), "%%%" + getValue().toString() + "%%%");
  }

}
