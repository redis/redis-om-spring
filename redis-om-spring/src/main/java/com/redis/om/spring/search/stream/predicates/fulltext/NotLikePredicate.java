package com.redis.om.spring.search.stream.predicates.fulltext;

import java.lang.reflect.Field;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.Values;

public class NotLikePredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private T value;

  public NotLikePredicate(Field field, T value) {
    super(field);
    this.value = value;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.NOT_LIKE;
  }

  public T getValue() {
    return value;
  }
  
  @Override
  public Node apply(Node root) {
    return QueryBuilder.intersect(root).add(QueryBuilder.disjunct(getField().getName(), Values.value("%%%"+value.toString()+"%%%")));
  }

}
