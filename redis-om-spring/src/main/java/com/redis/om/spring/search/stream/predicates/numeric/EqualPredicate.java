package com.redis.om.spring.search.stream.predicates.numeric;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.jedis.JedisValues;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private final T value;

  public EqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  public T getValue() {
    return value;
  }

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
