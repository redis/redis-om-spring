package com.redis.om.spring.search.stream.predicates.fulltext;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import org.apache.commons.lang3.ObjectUtils;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

public class NotContainingPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final T value;

  public NotContainingPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    return ObjectUtils.isNotEmpty(getValue()) ? QueryBuilders.intersect(root)
        .add(QueryBuilders.disjunct(getSearchAlias(), Values.value("*" + QueryUtils.escape(getValue().toString(), true) + "*"))) : root;
  }

}
