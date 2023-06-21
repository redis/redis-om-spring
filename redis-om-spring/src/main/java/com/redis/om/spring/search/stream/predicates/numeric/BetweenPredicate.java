package com.redis.om.spring.search.stream.predicates.numeric;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.jedis.JedisValues;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

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
    boolean paramsPresent = isNotEmpty(getMin()) && isNotEmpty(getMax());
    if (!paramsPresent)
      return root;
    Class<?> cls = min.getClass();
    if (cls == LocalDate.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.between((LocalDate) min, (LocalDate) max));
    } else if (cls == Date.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.between((Date) min, (Date) max));
    } else if (cls == LocalDateTime.class) {
      return QueryBuilders.intersect(root)
          .add(getSearchAlias(), JedisValues.between((LocalDateTime) min, (LocalDateTime) max));
    } else if (cls == Instant.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), JedisValues.between((Instant) min, (Instant) max));
    } else if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(),
          Values.between(Integer.parseInt(getMin().toString()), Integer.parseInt(getMax().toString())));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(),
          Values.between(Long.parseLong(getMin().toString()), Long.parseLong(getMax().toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(),
          Values.between(Double.parseDouble(getMin().toString()), Double.parseDouble(getMax().toString())));
    } else {
      return root;
    }
  }
}
