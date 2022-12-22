package com.redis.om.spring.search.stream.predicates.fulltext;

import java.lang.reflect.Field;
import java.util.List;
import java.util.StringJoiner;

import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private List<T> values;

  public InPredicate(Field field, List<T> values) {
    super(field);
    this.values = values;
  }

  public List<T> getValues() {
    return values;
  }

  @Override
  public Node apply(Node root) {
    StringJoiner sj = new StringJoiner(" | ");
    for (Object value : getValues()) {
      sj.add(value.toString());
    }

    return QueryBuilders.intersect(root).add(getField().getName(), sj.toString());
  }

}
