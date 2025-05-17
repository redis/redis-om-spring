package com.redis.om.spring.search.stream.predicates.tag;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;

public class EndsWithPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final T value;

  public EndsWithPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    if (isEmpty(getValue()))
      return root;
    if (Iterable.class.isAssignableFrom(getValue().getClass())) {
      Iterable<?> values = (Iterable<?>) getValue();
      QueryNode and = QueryBuilders.intersect();
      for (Object v : values) {
        and.add(getSearchAlias(), "{*" + QueryUtils.escape(v.toString(), true) + "}");
      }
      return QueryBuilders.intersect(root, and);
    } else {
      return QueryBuilders.intersect(root).add(getSearchAlias(), "{*" + QueryUtils.escape(value.toString(),
          true) + "}");
    }
  }

}
