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
 * A numeric comparison predicate that filters entities where a numeric field value
 * is greater than a specified value.
 * 
 * <p>This predicate supports various numeric and temporal types including:
 * {@link Integer}, {@link Long}, {@link Double}, {@link BigDecimal},
 * {@link LocalDate}, {@link LocalDateTime}, {@link Date}, and {@link Instant}.</p>
 * 
 * <p>The predicate generates Redis range queries in the format: {@code @field:[(value +inf]}
 * to match values greater than the specified threshold.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find expensive products (price > $100)
 * entityStream.filter(Product$.PRICE.gt(100.0))
 * 
 * // Find future events (after today)
 * entityStream.filter(Event$.DATE.gt(LocalDate.now()))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (must be a numeric or temporal type)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see GreaterThanOrEqualPredicate
 * @see LessThanPredicate
 */
public class GreaterThanPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  /** The threshold value for comparison */
  private final T value;

  /**
   * Creates a new GreaterThanPredicate for the specified field and threshold.
   * 
   * @param field the field accessor for the target numeric field
   * @param value the threshold value (field must be greater than this)
   */
  public GreaterThanPredicate(SearchFieldAccessor field, T value) {
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

  /**
   * Applies this greater-than predicate to the given query node.
   * 
   * <p>This method generates a Redis range query that matches documents where
   * the field value is greater than the specified threshold. The query format
   * creates an open range from the threshold to positive infinity.</p>
   * 
   * <p>If the value is empty or null, the predicate is ignored and the original
   * root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the greater-than condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    if (isEmpty(getValue()))
      return root;
    Class<?> cls = value.getClass();
    if (cls == LocalDate.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.gt((LocalDate) getValue()));
    } else if (cls == Date.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.gt((Date) getValue()));
    } else if (cls == LocalDateTime.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.gt((LocalDateTime) getValue()));
    } else if (cls == Instant.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.gt((Instant) getValue()));
    } else if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.gt(Integer.parseInt(getValue().toString())));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.gt(Long.parseLong(getValue().toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.gt(Double.parseDouble(getValue().toString())));
    } else if (cls == BigDecimal.class) {
      BigDecimal bigDecimal = (BigDecimal) getValue();
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.gt(bigDecimal.doubleValue()));
    } else {
      return root;
    }
  }
}
