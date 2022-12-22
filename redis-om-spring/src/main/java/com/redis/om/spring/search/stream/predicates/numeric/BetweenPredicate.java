package com.redis.om.spring.search.stream.predicates.numeric;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

public class BetweenPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final T min;
  private final T max;

  public BetweenPredicate(SearchFieldAccessor field, T min, T max) {
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
    Class<?> cls = min.getClass();
    if (cls == LocalDate.class) {
      LocalDate minLocalDate = (LocalDate) min;
      LocalDate maxLocalDate = (LocalDate) max;
      Instant minInstant = minLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
      Instant maxInstant = maxLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
      long minUnixTime = minInstant.getEpochSecond();
      long maxUnixTime = maxInstant.getEpochSecond();
      return QueryBuilders.intersect(root).add(getSearchAlias(),
          Values.between(Double.parseDouble(Long.toString(minUnixTime)), Double.parseDouble(Long.toString(maxUnixTime))));
    } else if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(),
          Values.between(Integer.parseInt(getMin().toString()), Integer.parseInt(getMax().toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(),
          Values.between(Double.parseDouble(getMin().toString()), Double.parseDouble(getMax().toString())));
    } else {
      return root;
    }
  }
}
