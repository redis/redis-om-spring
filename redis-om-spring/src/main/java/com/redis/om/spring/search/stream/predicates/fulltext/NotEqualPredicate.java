package com.redis.om.spring.search.stream.predicates.fulltext;

import org.apache.commons.lang3.ObjectUtils;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

/**
 * Predicate for performing "not equal" operations on full-text indexed fields.
 * This predicate excludes documents where the specified field exactly matches
 * the given value. It uses RediSearch's full-text search capabilities with
 * exact phrase matching and then negates the result.
 *
 * @param <E> the entity type being queried
 * @param <T> the type of the value being compared
 */
public class NotEqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private final T value;

  /**
   * Constructs a NotEqualPredicate for the specified field and value.
   *
   * @param field the search field accessor for the field to be queried
   * @param value the value that should not equal the field value
   */
  public NotEqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Gets the value that should not equal the field value.
   *
   * @return the value to exclude from exact matches
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies the "not equal" predicate to the query node tree.
   * Creates a disjunct (negated) query that excludes documents where the field
   * exactly matches the specified value using phrase matching.
   *
   * @param root the root query node to which this predicate will be applied
   * @return the modified query node with the "not equal" condition applied,
   *         or the original root if the value is empty
   */
  @Override
  public Node apply(Node root) {
    return ObjectUtils.isNotEmpty(getValue()) ?
        QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.value("\"" + getValue()
            .toString() + "\""))) :
        root;
  }

}
