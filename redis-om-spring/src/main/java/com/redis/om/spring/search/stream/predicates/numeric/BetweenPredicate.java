package com.redis.om.spring.search.stream.predicates.numeric;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

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
 * A numeric range predicate that filters entities where a numeric field value
 * falls between a specified minimum and maximum value (inclusive).
 * 
 * <p>This predicate supports various numeric and temporal types including:
 * {@link Integer}, {@link Long}, {@link Double}, {@link BigDecimal},
 * {@link LocalDate}, {@link LocalDateTime}, {@link Date}, and {@link Instant}.</p>
 * 
 * <p>The predicate generates Redis range queries in the format: {@code @field:[min max]}
 * which includes both the minimum and maximum values in the search results.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find products with prices between $10 and $50
 * entityStream.filter(Product$.PRICE.between(10.0, 50.0))
 * 
 * // Find events between two dates
 * entityStream.filter(Event$.DATE.between(startDate, endDate))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (must be a numeric or temporal type)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see redis.clients.jedis.search.querybuilder.Values#between(double, double)
 */
public class BetweenPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  /** The minimum value of the range (inclusive) */
  private final T min;

  /** The maximum value of the range (inclusive) */
  private final T max;

  /**
   * Creates a new BetweenPredicate for the specified field and range.
   * 
   * @param field the field accessor for the target numeric field
   * @param min   the minimum value of the range (inclusive)
   * @param max   the maximum value of the range (inclusive)
   */
  public BetweenPredicate(SearchFieldAccessor field, T min, T max) {
    super(field);
    this.min = min;
    this.max = max;
  }

  /**
   * Returns the minimum value of the range.
   * 
   * @return the minimum value (inclusive)
   */
  public T getMin() {
    return min;
  }

  /**
   * Returns the maximum value of the range.
   * 
   * @return the maximum value (inclusive)
   */
  public T getMax() {
    return max;
  }

  /**
   * Applies this between predicate to the given query node.
   * 
   * <p>This method generates a Redis range query that matches documents where
   * the field value falls between the minimum and maximum values (inclusive).
   * The specific query format depends on the field type:</p>
   * 
   * <ul>
   * <li>For numeric types: {@code @field:[min max]}</li>
   * <li>For date/time types: converted to epoch timestamps</li>
   * </ul>
   * 
   * <p>If either the minimum or maximum value is empty, the predicate is
   * ignored and the original root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the range condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    boolean paramsPresent = isNotEmpty(getMin()) && isNotEmpty(getMax());
    if (!paramsPresent)
      return root;
    Class<?> cls = min.getClass();
    if (cls == LocalDate.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.between((LocalDate) min, (LocalDate) max));
    } else if (cls == Date.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.between((Date) min, (Date) max));
    } else if (cls == LocalDateTime.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.between((LocalDateTime) min,
          (LocalDateTime) max));
    } else if (cls == Instant.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.between((Instant) min, (Instant) max));
    } else if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.between(Integer.parseInt(getMin().toString()),
          Integer.parseInt(getMax().toString())));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.between(Long.parseLong(getMin().toString()),
          Long.parseLong(getMax().toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.between(Double.parseDouble(getMin().toString()),
          Double.parseDouble(getMax().toString())));
    } else if (cls == BigDecimal.class) {
      BigDecimal min = (BigDecimal) getMin();
      BigDecimal max = (BigDecimal) getMax();
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.between(min.doubleValue(), max.doubleValue()));
    } else {
      return root;
    }
  }
}
