package com.redis.om.spring.search.stream.predicates.tag;

import java.lang.reflect.Field;
import java.util.stream.StreamSupport;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.QueryNode;
import io.redisearch.querybuilder.Values;

public class NotEqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;

  public NotEqualPredicate(Field field, T value) {
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
      QueryNode and = QueryBuilder.intersect();
      Iterable<?> values = (Iterable<?>) value;
      
      StreamSupport.stream(values.spliterator(), false)
          .map(v -> Values.value("{" + v.toString() + "}"))
          .forEach(value -> and.add(QueryBuilder.disjunct(getField().getName(), value)));

      return QueryBuilder.intersect(root, and);
    } else {
      return QueryBuilder.intersect(root)
          .add(QueryBuilder.disjunct(getField().getName(), "{" + value.toString() + "}"));
    }
  }

}
