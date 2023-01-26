package com.redis.om.spring.search.stream.predicates.tag;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;

public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private final T value;

  public EqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = QueryUtils.escape(value);
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    if (Iterable.class.isAssignableFrom(getValue().getClass())) {
      Iterable<?> values = (Iterable<?>) getValue();
      QueryNode and = QueryBuilders.intersect();
      for (Object v : values) {
        and.add(getSearchAlias(), "{" + v.toString() + "}");
      }
      return QueryBuilders.intersect(root, and);
    } else {
      return QueryBuilders.intersect(root).add(getSearchAlias(), "{" + value.toString() + "}");
    }
  }

}
