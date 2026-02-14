package com.redis.om.spring.search.stream.predicates.fulltext;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * Represents an "is missing" predicate for field searches.
 * This predicate generates RediSearch queries that match documents where the specified
 * field is missing or not present in the document. This is useful for finding documents
 * that lack certain fields or have null values.
 *
 * @param <E> the entity type
 * @param <T> the field value type
 */
public class IsMissingPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  /**
   * Constructs a new "is missing" predicate.
   *
   * @param field the search field accessor for the field to check for absence
   */
  public IsMissingPredicate(SearchFieldAccessor field) {
    super(field);
  }

  @Override
  public Node apply(Node root) {
    String query = String.format("ismissing(@%s)", getSearchAlias());

    Node isMissingNode = new Node() {
      @Override
      public String toString() {
        return query;
      }

      @Override
      public String toString(Parenthesize mode) {
        return query;
      }
    };

    // Combine with the existing root using AND (intersect)
    // If root is empty, just return the isMissing node
    return root.toString().isBlank() ? isMissingNode : QueryBuilders.intersect(root, isMissingNode);
  }
}