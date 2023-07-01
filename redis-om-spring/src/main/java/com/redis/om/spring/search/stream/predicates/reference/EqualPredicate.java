package com.redis.om.spring.search.stream.predicates.reference;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.jedis.JedisValues;
import org.apache.commons.lang3.ObjectUtils;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static com.redis.om.spring.util.ObjectUtils.getIdFieldForEntity;

public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private final Object referenceId;
  private final T value;

  public EqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
    this.referenceId = getIdFieldForEntity(value);
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    Class<?> cls = referenceId.getClass();
    if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Integer.parseInt(referenceId.toString())));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Long.parseLong(referenceId.toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Double.parseDouble(referenceId.toString())));
    } else if (CharSequence.class.isAssignableFrom(cls)) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), "{" + referenceId + "}");
    } else {
      return root;
    }
  }
}
