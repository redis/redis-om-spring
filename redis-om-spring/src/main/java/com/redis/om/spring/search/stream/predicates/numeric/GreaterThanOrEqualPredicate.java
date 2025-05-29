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
 * Represents a "greater than or equal to" predicate for numeric field searches.
 * This predicate generates RediSearch queries that match documents where the specified
 * field's value is greater than or equal to the provided value.
 *
 * @param <E> the entity type
 * @param <T> the field value type
 */
public class GreaterThanOrEqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final T value;

  /**
   * Constructs a new greater than or equal predicate.
   *
   * @param field the search field accessor
   * @param value the value to compare against
   */
  public GreaterThanOrEqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Gets the comparison value for this predicate.
   *
   * @return the value used in the greater than or equal comparison
   */
  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    if (isEmpty(getValue()))
      return root;
    Class<?> cls = value.getClass();
    if (cls == LocalDate.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.ge((LocalDate) getValue()));
    } else if (cls == Date.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.ge((Date) getValue()));
    } else if (cls == LocalDateTime.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.ge((LocalDateTime) getValue()));
    } else if (cls == Instant.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.ge((Instant) getValue()));
    } else if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.ge(Integer.parseInt(getValue().toString())));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.ge(Long.parseLong(getValue().toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.ge(Double.parseDouble(getValue().toString())));
    } else if (cls == BigDecimal.class) {
      BigDecimal bigDecimal = (BigDecimal) getValue();
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.ge(bigDecimal.doubleValue()));
    } else {
      return root;
    }
  }

}
