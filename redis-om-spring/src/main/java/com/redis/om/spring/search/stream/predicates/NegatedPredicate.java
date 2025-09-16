package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;

import redis.clients.jedis.search.Schema.FieldType;
import redis.clients.jedis.search.querybuilder.Node;

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
    // Get the node from the wrapped predicate
    Node predicateNode = predicate.apply(root);

    // If the predicate generates a custom query string, negate it
    String query = predicateNode.toString();

    // For special queries like "ismissing", add the negation operator
    String negatedQuery = "-" + query;

    return new Node() {
      @Override
      public String toString() {
        return negatedQuery;
      }

      @Override
      public String toString(Parenthesize mode) {
        return negatedQuery;
      }
    };
  }

  @Override
  public SearchFieldPredicate<E, T> negate() {
    // Double negation returns the original predicate
    return predicate;
  }
}