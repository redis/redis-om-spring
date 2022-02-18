package com.redis.om.spring.search.stream.predicates.numeric;

import java.lang.reflect.Field;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.Values;

public class LessThanPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private T value;

  public LessThanPredicate(Field field, T value) {
    super(field);
    this.value = value;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.LESS_THAN;
  }

  public T getValue() {
    return value;
  }
  
  @Override
  public Node apply(Node root) {
    Class<?> cls = ObjectUtils.getNumericClassFor(value.toString());
    if (cls == Integer.class) {
      return QueryBuilder.intersect(root).add(getField().getName(), Values.lt(Integer.valueOf(value.toString())));
    } else {
      return QueryBuilder.intersect(root).add(getField().getName(), Values.lt(Double.valueOf(value.toString())));
    }
  }

}
