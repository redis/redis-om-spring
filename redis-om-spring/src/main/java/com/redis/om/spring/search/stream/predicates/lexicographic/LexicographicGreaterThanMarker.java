package com.redis.om.spring.search.stream.predicates.lexicographic;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;

/**
 * A marker predicate for lexicographic greater-than operations.
 * This predicate doesn't apply the query itself but signals to the SearchStream
 * that a lexicographic comparison is needed.
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (must be String or convertible to String)
 * 
 * @since 1.0
 */
public class LexicographicGreaterThanMarker<E, T> extends BaseAbstractPredicate<E, T> implements
    LexicographicPredicate {
  private final T value;

  /**
   * Creates a new LexicographicGreaterThanMarker for the specified field and threshold.
   * 
   * @param field the field accessor for the target string field
   * @param value the threshold value (field must be lexicographically greater than this)
   */
  public LexicographicGreaterThanMarker(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Returns the threshold value for comparison.
   * 
   * @return the threshold value
   */
  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    // This is a marker predicate - actual processing happens in SearchStream
    throw new UnsupportedOperationException("Lexicographic predicates must be processed by SearchStream");
  }
}