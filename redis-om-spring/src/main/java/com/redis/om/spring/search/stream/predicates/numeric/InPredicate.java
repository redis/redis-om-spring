package com.redis.om.spring.search.stream.predicates.numeric;

import java.time.*;
import java.util.Date;
import java.util.List;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.QueryNode;
import io.redisearch.querybuilder.Values;

public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private List<T> values;

  public InPredicate(SearchFieldAccessor field, List<T> values) {
    super(field);
    this.values = values;
  }

  public List<T> getValues() {
    return values;
  }

  @Override
  public Node apply(Node root) {
    QueryNode or = QueryBuilder.union();

    Class<?> cls = values.get(0).getClass();

    for (Object value : getValues()) {
      if (cls == Integer.class) {
        or.add(getSearchAlias(), Values.eq(Integer.parseInt(value.toString())));
      } else if (cls == LocalDate.class) {
        LocalDate localDate = (LocalDate) value;
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        long unixTime = instant.getEpochSecond();
        or.add(getSearchAlias(), Values.eq(unixTime));
      } else if (cls == Date.class) {
        Date date = (Date) value;
        Instant instant = date.toInstant();
        long unixTime = instant.getEpochSecond();
        or.add(getSearchAlias(), Values.eq(unixTime));
      } else if (cls == LocalDateTime.class) {
        LocalDateTime localDateTime = (LocalDateTime) value;
        Instant instant = localDateTime.toInstant(ZoneOffset.of(ZoneId.systemDefault().getId()));
        long unixTime = instant.getEpochSecond();
        or.add(getSearchAlias(), Values.eq(unixTime));
      } else if (cls == Instant.class) {
        Instant instant = (Instant) value;
        long unixTime = instant.getEpochSecond();
        or.add(getSearchAlias(), Values.eq(unixTime));
      } else {
        or.add(getSearchAlias(), Values.eq(Double.valueOf(value.toString())));
      }
    }

    return QueryBuilder.intersect(root, or);
  }

}
