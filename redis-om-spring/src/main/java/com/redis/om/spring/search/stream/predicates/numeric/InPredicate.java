package com.redis.om.spring.search.stream.predicates.numeric;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.jedis.JedisValues;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;
import redis.clients.jedis.search.querybuilder.Values;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<T> values;

  public InPredicate(SearchFieldAccessor field, List<T> values) {
    super(field);
    this.values = values;
  }

  public List<T> getValues() {
    return values;
  }

  @Override
  public Node apply(Node root) {
    if (isEmpty(getValues()))
      return root;
    QueryNode or = QueryBuilders.union();

    Class<?> cls = values.get(0).getClass();

    for (Object value : getValues()) {
      if (cls == Integer.class) {
        or.add(getSearchAlias(), Values.eq(Integer.parseInt(value.toString())));
      } else if (cls == Long.class) {
        or.add(getSearchAlias(), Values.eq(Long.parseLong(value.toString())));
      } else if (cls == LocalDate.class) {
        or.add(getSearchAlias(), JedisValues.eq((LocalDate) value));
      } else if (cls == Date.class) {
        or.add(getSearchAlias(), JedisValues.eq((Date) value));
      } else if (cls == LocalDateTime.class) {
        or.add(getSearchAlias(), JedisValues.eq((LocalDateTime) value));
      } else if (cls == Instant.class) {
        or.add(getSearchAlias(), JedisValues.eq((Instant) value));
      } else {
        or.add(getSearchAlias(), Values.eq(Double.parseDouble(value.toString())));
      }
    }

    return QueryBuilders.intersect(root, or);
  }

}
