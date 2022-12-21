package com.redis.om.spring.search.stream.predicates.tag;

import java.lang.reflect.Field;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.QueryNode;

public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;

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
      QueryNode and = QueryBuilder.intersect();
      for (Object v : values) {
        and.add(getSearchAlias(), "{" + v.toString() + "}");
      }
      return QueryBuilder.intersect(root, and);
    } else {
      return QueryBuilder.intersect(root).add(getSearchAlias(), "{" + value.toString() + "}");
    }
  }

}
