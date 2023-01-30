package com.redis.om.spring.search.stream.predicates.fulltext;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

public class NotLikePredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final T value;

  public NotLikePredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    return QueryBuilders.intersect(root)
        .add(QueryBuilders.disjunct(getSearchAlias(), Values.value("%%%" + getValue().toString() + "%%%")));
  }

}
