package com.redis.om.spring.search.stream.predicates.numeric;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.jedis.JedisValues;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;
import redis.clients.jedis.search.querybuilder.Values;

/**
 * Represents an "in" predicate for numeric field searches.
 * This predicate generates RediSearch queries that match documents where the specified
 * numeric field equals any of the provided values. Supports various numeric types including
 * integers, longs, doubles, BigDecimal, and date/time types.
 *
 * @param <E> the entity type
 * @param <T> the field value type
 */
public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<T> values;

  /**
   * Constructs a new numeric "in" predicate.
   *
   * @param field  the search field accessor
   * @param values the list of numeric values to search for
   */
  public InPredicate(SearchFieldAccessor field, List<T> values) {
    super(field);
    this.values = values;
  }

  /**
   * Gets the list of numeric values to search for in this predicate.
   *
   * @return the list of values used in the "in" comparison
   */
  public List<T> getValues() {
    return values;
  }

  @Override
  public Node apply(Node root) {
    if (isEmpty(getValues())) {
      return root;
    }

    QueryNode or = QueryBuilders.union();

    for (Object value : getValues()) {
      processValue(or, value);
    }

    return QueryBuilders.intersect(root, or);
  }

  private void processValue(QueryNode or, Object value) {
    if (value == null) {
      return;
    }

    if (value instanceof Collection) {
      ((Collection<?>) value).forEach(v -> processValue(or, v));
    } else if (value instanceof Iterable) {
      ((Iterable<?>) value).forEach(v -> processValue(or, v));
    } else if (value.getClass().isArray()) {
      Arrays.stream((Object[]) value).forEach(v -> processValue(or, v));
    } else {
      addValueToQuery(or, value);
    }
  }

  private void addValueToQuery(QueryNode or, Object value) {
    Class<?> valueClass = value.getClass();

    if (valueClass == Integer.class) {
      or.add(getSearchAlias(), Values.eq((Integer) value));
    } else if (valueClass == Long.class) {
      or.add(getSearchAlias(), Values.eq((Long) value));
    } else if (valueClass == LocalDate.class) {
      or.add(getSearchAlias(), JedisValues.eq((LocalDate) value));
    } else if (valueClass == Date.class) {
      or.add(getSearchAlias(), JedisValues.eq((Date) value));
    } else if (valueClass == LocalDateTime.class) {
      or.add(getSearchAlias(), JedisValues.eq((LocalDateTime) value));
    } else if (valueClass == Instant.class) {
      or.add(getSearchAlias(), JedisValues.eq((Instant) value));
    } else if (valueClass == BigDecimal.class) {
      or.add(getSearchAlias(), Values.eq(((BigDecimal) value).doubleValue()));
    } else {
      or.add(getSearchAlias(), Values.eq(Double.parseDouble(value.toString())));
    }
  }

}
