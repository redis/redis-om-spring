package com.redis.om.spring.search.stream.predicates.tag;

import java.util.List;
import java.util.stream.StreamSupport;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;
import redis.clients.jedis.search.querybuilder.Values;

public class NotEqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;
  private Iterable<?> values;

  public NotEqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = QueryUtils.escape(value);
  }

  public NotEqualPredicate(SearchFieldAccessor field, List<String> list) {
    super(field);
    this.values = list.stream().map(QueryUtils::escape).toList();
  }

  public Iterable<?> getValues() {
    return value != null ? (Iterable<?>) value : values;
  }

  @Override
  public Node apply(Node root) {
    QueryNode and = QueryBuilders.intersect();

    StreamSupport.stream(getValues().spliterator(), false) //
        .map(v -> Values.value("{" + v.toString() + "}"))
        .forEach(value -> and.add(QueryBuilders.disjunct(getSearchAlias(), value)));

    return QueryBuilders.intersect(root, and);
  }

}
