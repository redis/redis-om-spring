package com.redis.om.spring.search.stream.predicates.tag;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.util.List;
import java.util.stream.StreamSupport;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;
import redis.clients.jedis.search.querybuilder.Values;

/**
 * Predicate for performing "not equal" operations on tag-indexed fields.
 * This predicate excludes documents where the specified tag field contains
 * any of the given tag values. It supports both single values and collections
 * of values, using RediSearch tag syntax for exact matching.
 *
 * @param <E> the entity type being queried
 * @param <T> the type of the tag value being compared
 */
public class NotEqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private T value;
  private Iterable<?> values;

  /**
   * Constructs a NotEqualPredicate for the specified field and single tag value.
   *
   * @param field the search field accessor for the tag field to be queried
   * @param value the tag value that should not equal the field value
   */
  public NotEqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Constructs a NotEqualPredicate for the specified field and list of tag values.
   *
   * @param field the search field accessor for the tag field to be queried
   * @param list  the list of tag values that should not equal the field value
   */
  public NotEqualPredicate(SearchFieldAccessor field, List<String> list) {
    super(field);
    this.values = list.stream().map(QueryUtils::escape).toList();
  }

  /**
   * Gets the tag values that should not equal the field value.
   *
   * @return the tag values to exclude from matches, either as a single value or collection
   */
  public Iterable<?> getValues() {
    return value != null ? (Iterable<?>) value : values;
  }

  /**
   * Applies the "not equal" predicate to the query node tree.
   * Creates a disjunct (negated) query that excludes documents where the tag field
   * contains any of the specified tag values. Each tag value is wrapped in tag syntax
   * for exact matching.
   *
   * @param root the root query node to which this predicate will be applied
   * @return the modified query node with the tag "not equal" condition applied,
   *         or the original root if no values are provided
   */
  @Override
  public Node apply(Node root) {
    if (isEmpty(getValues()))
      return root;
    QueryNode and = QueryBuilders.intersect();

    StreamSupport.stream(getValues().spliterator(), false) //
        .map(v -> Values.value("{\"" + v.toString() + "\"}")).forEach(val -> and.add(QueryBuilders.disjunct(
            getSearchAlias(), val)));

    return QueryBuilders.intersect(root, and);
  }

}
