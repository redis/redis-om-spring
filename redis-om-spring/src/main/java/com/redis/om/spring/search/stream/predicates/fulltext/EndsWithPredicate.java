package com.redis.om.spring.search.stream.predicates.fulltext;

import org.apache.commons.lang3.ObjectUtils;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * Fulltext field ends with predicate.
 *
 * @param <E> the entity type
 * @param <T> the field type
 */
public class EndsWithPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final T value;

  /**
   * Creates a new ends with predicate.
   *
   * @param field the search field
   * @param value the value to match
   */
  public EndsWithPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Gets the value being matched.
   *
   * @return the value
   */
  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    return ObjectUtils.isNotEmpty(getValue()) ?
        QueryBuilders.intersect(root).add(getSearchAlias(), "*" + QueryUtils.escape(getValue().toString(), true)) :
        root;
  }

}
