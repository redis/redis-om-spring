package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;

import redis.clients.jedis.search.Schema.FieldType;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * A predicate that negates another search field predicate.
 * This class wraps a {@link SearchFieldPredicate} and applies logical negation to it
 * in Redis search queries.
 *
 * <p>This predicate is used when calling {@code negate()} on a search predicate,
 * ensuring that the negation is properly handled in the Redis search query.</p>
 *
 * @param <E> the entity type being filtered
 * @param <T> the field type of the predicate
 */
public class NegatedPredicate<E, T> implements SearchFieldPredicate<E, T> {

  private final SearchFieldPredicate<E, T> predicate;

  /**
   * Creates a new negated predicate.
   *
   * @param predicate the predicate to negate
   */
  public NegatedPredicate(SearchFieldPredicate<E, T> predicate) {
    this.predicate = predicate;
  }

  @Override
  public boolean test(T t) {
    return !predicate.test(t);
  }

  @Override
  public FieldType getSearchFieldType() {
    return predicate.getSearchFieldType();
  }

  @Override
  public Field getField() {
    return predicate.getField();
  }

  @Override
  public String getSearchAlias() {
    return predicate.getSearchAlias();
  }

  @Override
  public Node apply(Node root) {
    // Apply the predicate to an empty root to get just the predicate's condition
    Node predicateNode = predicate.apply(QueryBuilders.union());

    // Negate the predicate's condition
    String query = predicateNode.toString();
    String negatedQuery = "-" + query;

    Node negatedNode = new Node() {
      @Override
      public String toString() {
        return negatedQuery;
      }

      @Override
      public String toString(Parenthesize mode) {
        return negatedQuery;
      }
    };

    // Combine the negated condition with the original root using AND
    // If root is empty, just return the negated node
    return root.toString().isBlank() ? negatedNode : QueryBuilders.intersect(root, negatedNode);
  }

  @Override
  public SearchFieldPredicate<E, T> negate() {
    // Double negation returns the original predicate
    return predicate;
  }
}