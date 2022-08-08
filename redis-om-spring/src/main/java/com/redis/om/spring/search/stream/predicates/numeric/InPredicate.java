package com.redis.om.spring.search.stream.predicates.numeric;

import java.lang.reflect.Field;
import java.util.List;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.QueryNode;
import io.redisearch.querybuilder.Values;

public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private List<T> values;

  public InPredicate(Field field, List<T> values) {
    super(field);
    this.values = values;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.IN;
  }

  public List<T> getValues() {
    return values;
  }

  @Override
  public Node apply(Node root) {
    QueryNode or = QueryBuilder.union();

    Class<?> cls = values.get(0).getClass();

    for (Object value : values) {
      if (cls == Integer.class) {
        or.add(getField().getName(), Values.eq(Integer.valueOf(value.toString())));
      } else {
        or.add(getField().getName(), Values.eq(Double.valueOf(value.toString())));
      }
    }

    return QueryBuilder.intersect(root, or);
  }

}
