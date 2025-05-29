package com.redis.om.spring.search.stream.predicates.fulltext;

import org.apache.commons.lang3.ObjectUtils;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * Predicate for full-text equality searches. This predicate matches documents where
 * the specified field contains the exact value provided.
 *
 * @param <E> the entity type
 * @param <T> the value type
 */
public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private final T value;

  /**
   * Constructs a new EqualPredicate.
   *
   * @param field the search field accessor
   * @param value the value to match
   */
  public EqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Gets the value being matched by this predicate.
   *
   * @return the value to match
   */
  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    return ObjectUtils.isNotEmpty(getValue()) ?
        QueryBuilders.intersect(root).add(getSearchAlias(), "\"" + getValue().toString() + "\"") :
        root;
  }

}
