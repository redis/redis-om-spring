package com.redis.om.spring.search.stream.predicates.numeric;

import java.lang.reflect.Field;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.Values;

public class BetweenPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private T min;
  private T max;

  public BetweenPredicate(Field field, T min, T max) {
    super(field);
    this.min = min;
    this.max = max;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.BETWEEN;
  }

  public T getMin() {
    return min;
  }
  
  public T getMax() {
    return max;
  }
  
  @Override
  public Node apply(Node root) {
    Class<?> cls = ObjectUtils.getNumericClassFor(min.toString());
    if (cls == Integer.class) {
      return QueryBuilder.intersect(root).add(getField().getName(), Values.between(Integer.valueOf(min.toString()), Integer.valueOf(max.toString())));
    } else {
      return QueryBuilder.intersect(root).add(getField().getName(), Values.between(Double.valueOf(min.toString()), Double.valueOf(max.toString())));
    }
  }
}
