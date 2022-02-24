package com.redis.om.spring.search.stream.predicates.tag;

import java.lang.reflect.Field;
import java.util.List;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.QueryNode;

public class ContainsAllPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private List<String> values;

  public ContainsAllPredicate(Field field, List<String> list) {
    super(field);
    this.values = list;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.CONTAINS_ALL;
  }

  public List<String> getValues() {
    return values;
  }

  @Override
  public Node apply(Node root) {
    QueryNode and = QueryBuilder.intersect();
    for (String value : values) {
      and.add(getField().getName(), "{" + value + "}");
    }

    return QueryBuilder.intersect(root, and);
  }
}
