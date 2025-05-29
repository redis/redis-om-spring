package com.redis.om.spring.search.stream.predicates.numeric;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.jedis.JedisValues;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

/**
 * A numeric inequality predicate that filters entities where a numeric field value
 * does not equal a specified value.
 * 
 * <p>This predicate supports various numeric and temporal types including:
 * {@link Integer}, {@link Long}, {@link Double}, {@link BigDecimal},
 * {@link LocalDate}, {@link LocalDateTime}, {@link Date}, and {@link Instant}.</p>
 * 
 * <p>The predicate generates Redis inequality queries using negation (disjunct)
 * to exclude documents that match the specified value.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find products that are not priced at $25.99
 * entityStream.filter(Product$.PRICE.ne(25.99))
 * 
 * // Find events not on Christmas
 * entityStream.filter(Event$.DATE.ne(LocalDate.of(2023, 12, 25)))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (must be a numeric or temporal type)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see EqualPredicate
 */
public class NotEqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  /** The value to exclude from matching */
  private final T value;

  /**
   * Creates a new NotEqualPredicate for the specified field and value.
   * 
   * @param field the field accessor for the target numeric field
   * @param value the value to exclude from matching
   */
  public NotEqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Returns the value to exclude from matching.
   * 
   * @return the value that should not match
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies this inequality predicate to the given query node.
   * 
   * <p>This method generates a Redis inequality query using negation that
   * excludes documents where the field value equals the specified value.
   * The query uses disjunction to negate the equality condition.</p>
   * 
   * <p>If the value is empty or null, the predicate is ignored and the original
   * root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the inequality condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    if (isEmpty(getValue()))
      return root;
    Class<?> cls = value.getClass();
    if (cls == LocalDate.class) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), JedisValues.eq(
          (LocalDate) getValue())));
    } else if (cls == Date.class) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), JedisValues.eq(
          (Date) getValue())));
    } else if (cls == LocalDateTime.class) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), JedisValues.eq(
          (LocalDateTime) getValue())));
    } else if (cls == Instant.class) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), JedisValues.eq(
          (Instant) getValue())));
    } else if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.eq(Integer.parseInt(
          getValue().toString()))));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.eq(Long.parseLong(
          getValue().toString()))));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.eq(Double.parseDouble(
          getValue().toString()))));
    } else if (cls == BigDecimal.class) {
      BigDecimal bigDecimal = (BigDecimal) getValue();
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.eq(bigDecimal
          .doubleValue())));
    } else {
      return root;
    }
  }

}
