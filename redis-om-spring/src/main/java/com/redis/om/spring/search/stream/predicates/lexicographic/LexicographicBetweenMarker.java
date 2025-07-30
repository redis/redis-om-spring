package com.redis.om.spring.search.stream.predicates.lexicographic;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;

/**
 * A marker predicate for lexicographic between operations.
 * This predicate doesn't apply the query itself but signals to the SearchStream
 * that a lexicographic range comparison is needed.
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (must be String or convertible to String)
 * 
 * @since 1.0
 */
public class LexicographicBetweenMarker<E, T> extends BaseAbstractPredicate<E, T> implements LexicographicPredicate {
  private final T min;
  private final T max;

  public LexicographicBetweenMarker(SearchFieldAccessor field, T min, T max) {
    super(field);
    this.min = min;
    this.max = max;
  }

  public T getMin() {
    return min;
  }

  public T getMax() {
    return max;
  }

  @Override
  public Node apply(Node root) {
    // This is a marker predicate - actual processing happens in SearchStream
    throw new UnsupportedOperationException("Lexicographic predicates must be processed by SearchStream");
  }
}