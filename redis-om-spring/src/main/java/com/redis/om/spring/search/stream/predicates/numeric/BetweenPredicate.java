package com.redis.om.spring.search.stream.predicates.numeric;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.Values;

public class BetweenPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private T min;
  private T max;

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
      Long minUnixTime = minInstant.getEpochSecond();
      Long maxUnixTime = maxInstant.getEpochSecond();
      return QueryBuilder.intersect(root).add(getSearchAlias(),
          Values.between(Double.valueOf(minUnixTime.toString()), Double.valueOf(maxUnixTime.toString())));
    } else if (cls == Integer.class) {
      return QueryBuilder.intersect(root).add(getSearchAlias(),
          Values.between(Integer.valueOf(getMin().toString()), Integer.valueOf(getMax().toString())));
    } else if (cls == Double.class) {
      return QueryBuilder.intersect(root).add(getSearchAlias(),
          Values.between(Double.valueOf(getMin().toString()), Double.valueOf(getMax().toString())));
    } else {
      return root;
    }
  }
}
