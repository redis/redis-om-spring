package com.redis.om.spring.search.stream.predicates.numeric;

import java.time.*;
import java.util.Date;

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
      long minUnixTime = minInstant.getEpochSecond();
      long maxUnixTime = maxInstant.getEpochSecond();
      return QueryBuilder.intersect(root).add(getSearchAlias(),
          Values.between(Double.parseDouble(Long.toString(minUnixTime)),
              Double.parseDouble(Long.toString(maxUnixTime))));
    } else if (cls == Date.class) {
      Date minLocalDate = (Date) min;
      Date maxLocalDate = (Date) max;
      Instant minInstant = minLocalDate.toInstant();
      Instant maxInstant = maxLocalDate.toInstant();
      long minUnixTime = minInstant.getEpochSecond();
      long maxUnixTime = maxInstant.getEpochSecond();
      return QueryBuilder.intersect(root).add(getSearchAlias(),
          Values.between(Double.parseDouble(Long.toString(minUnixTime)),
              Double.parseDouble(Long.toString(maxUnixTime))));
    } else if (cls == LocalDateTime.class) {
      LocalDateTime minLocalDateTime = (LocalDateTime) min;
      LocalDateTime maxLocalDateTime = (LocalDateTime) max;
      Instant minInstant = minLocalDateTime.toInstant(ZoneOffset.of(ZoneId.systemDefault().getId()));
      Instant maxInstant = maxLocalDateTime.toInstant(ZoneOffset.of(ZoneId.systemDefault().getId()));
      long minUnixTime = minInstant.getEpochSecond();
      long maxUnixTime = maxInstant.getEpochSecond();
      return QueryBuilder.intersect(root).add(getSearchAlias(),
          Values.between(Double.parseDouble(Long.toString(minUnixTime)),
              Double.parseDouble(Long.toString(maxUnixTime))));
    } else if (cls == Instant.class) {
      Instant minInstant = (Instant) min;
      Instant maxInstant = (Instant) max;
      long minUnixTime = minInstant.getEpochSecond();
      long maxUnixTime = maxInstant.getEpochSecond();
      return QueryBuilder.intersect(root).add(getSearchAlias(),
          Values.between(Double.parseDouble(Long.toString(minUnixTime)),
              Double.parseDouble(Long.toString(maxUnixTime))));
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
