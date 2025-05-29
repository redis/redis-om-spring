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
 * A numeric equality predicate that filters entities where a numeric field value
 * exactly matches a specified value.
 * 
 * <p>This predicate supports various numeric and temporal types including:
 * {@link Integer}, {@link Long}, {@link Double}, {@link BigDecimal},
 * {@link LocalDate}, {@link LocalDateTime}, {@link Date}, and {@link Instant}.</p>
 * 
 * <p>The predicate generates Redis equality queries for numeric fields, handling
 * type-specific conversions and formatting required by RediSearch.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find products with exactly $25.99 price
 * entityStream.filter(Product$.PRICE.eq(25.99))
 * 
 * // Find events on a specific date
 * entityStream.filter(Event$.DATE.eq(LocalDate.of(2023, 12, 25)))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (must be a numeric or temporal type)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see redis.clients.jedis.search.querybuilder.Values#eq(double)
 */
public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  /** The value to match against */
  private final T value;

  /**
   * Creates a new EqualPredicate for the specified field and value.
   * 
   * @param field the field accessor for the target numeric field
   * @param value the value to match against
   */
  public EqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Returns the value to match against.
   * 
   * @return the target value for equality comparison
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies this equality predicate to the given query node.
   * 
   * <p>This method generates a Redis equality query that matches documents where
   * the field value exactly equals the specified value. The query format depends
   * on the field type and uses appropriate type conversions.</p>
   * 
   * <p>If the value is empty or null, the predicate is ignored and the original
   * root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the equality condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    if (isEmpty(getValue()))
      return root;
    Class<?> cls = getValue().getClass();
    if (cls == LocalDate.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.eq((LocalDate) getValue()));
    } else if (cls == Date.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.eq((Date) getValue()));
    } else if (cls == LocalDateTime.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.eq((LocalDateTime) getValue()));
    } else if (cls == Instant.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.eq((Instant) getValue()));
    } else if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Integer.parseInt(getValue().toString())));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Long.parseLong(getValue().toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Double.parseDouble(getValue().toString())));
    } else if (cls == BigDecimal.class) {
      BigDecimal bigDecimal = (BigDecimal) getValue();
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(bigDecimal.doubleValue()));
    } else {
      return root;
    }
  }

}
