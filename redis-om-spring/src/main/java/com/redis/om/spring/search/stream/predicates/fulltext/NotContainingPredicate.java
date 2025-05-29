package com.redis.om.spring.search.stream.predicates.fulltext;

import org.apache.commons.lang3.ObjectUtils;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

/**
 * Predicate for performing "not containing" operations on full-text indexed fields.
 * This predicate excludes documents where the specified field contains the given value.
 * It uses RediSearch's full-text search capabilities with wildcard patterns to perform
 * substring matching and then negates the result.
 *
 * @param <E> the entity type being queried
 * @param <T> the type of the value being compared
 */
public class NotContainingPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final T value;

  /**
   * Constructs a NotContainingPredicate for the specified field and value.
   *
   * @param field the search field accessor for the field to be queried
   * @param value the value that should not be contained in the field
   */
  public NotContainingPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Gets the value that should not be contained in the field.
   *
   * @return the value to exclude from containing matches
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies the "not containing" predicate to the query node tree.
   * Creates a disjunct (negated) query that excludes documents where the field
   * contains the specified value using wildcard pattern matching.
   *
   * @param root the root query node to which this predicate will be applied
   * @return the modified query node with the "not containing" condition applied,
   *         or the original root if the value is empty
   */
  @Override
  public Node apply(Node root) {
    return ObjectUtils.isNotEmpty(getValue()) ?
        QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.value("*" + QueryUtils.escape(
            getValue().toString(), true) + "*"))) :
        root;
  }

}
