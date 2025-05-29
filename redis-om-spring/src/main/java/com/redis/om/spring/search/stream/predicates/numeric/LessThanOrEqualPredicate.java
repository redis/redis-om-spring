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
 * A predicate that represents a "less than or equal to" comparison for numeric and temporal values.
 * This predicate can handle various data types including dates, times, and numeric values,
 * converting them to appropriate Redis search query conditions.
 *
 * @param <E> the entity type being queried
 * @param <T> the type of the value being compared
 */
public class LessThanOrEqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final T value;

  /**
   * Constructs a new LessThanOrEqualPredicate with the specified field and value.
   *
   * @param field the search field accessor for the field to compare
   * @param value the value to compare against (must be less than or equal to this value)
   */
  public LessThanOrEqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Gets the value being compared against.
   *
   * @return the comparison value
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies the less than or equal to predicate to the given root node.
   * This method handles various data types and converts them to appropriate Redis search syntax.
   *
   * @param root the root query node to apply this predicate to
   * @return the modified query node with the less than or equal condition applied,
   *         or the original root node if the value is empty
   */
  @Override
  public Node apply(Node root) {
    if (isEmpty(getValue()))
      return root;
    Class<?> cls = value.getClass();
    if (cls == LocalDate.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.le((LocalDate) getValue()));
    } else if (cls == Date.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.le((Date) getValue()));
    } else if (cls == LocalDateTime.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.le((LocalDateTime) getValue()));
    } else if (cls == Instant.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.le((Instant) getValue()));
    } else if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.le(Integer.parseInt(getValue().toString())));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.le(Long.parseLong(getValue().toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.le(Double.parseDouble(getValue().toString())));
    } else if (cls == BigDecimal.class) {
      BigDecimal bigDecimal = (BigDecimal) getValue();
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.le(bigDecimal.doubleValue()));
    } else {
      return root;
    }
  }

}
