package com.redis.om.spring.search.stream.predicates.tag;

import java.lang.reflect.Field;
import java.util.List;
import java.util.StringJoiner;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.PredicateType;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;

public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private List<String> values;

  public InPredicate(Field field, List<String> list) {
    super(field);
    this.values = list;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.IN;
  }

  public List<String> getValues() {
    return values;
  }

  @Override
  public Node apply(Node root) {
    StringJoiner sj = new StringJoiner(" | ");
    for (Object value : values) {
      sj.add(value.toString());
    }

    return QueryBuilder.intersect(root).add(getField().getName(), "{" + sj.toString() + "}");
  }
}
