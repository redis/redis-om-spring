package com.redis.om.spring.search.stream.actions;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import redis.clients.jedis.json.Path2;

import java.util.function.Consumer;

public class NumIncrByAction<E> extends BaseAbstractAction implements Consumer<E> {
  private final Long value;

  public NumIncrByAction(SearchFieldAccessor field, Long value) {
    super(field);
    this.value = value;
  }

  @Override
  public void accept(E entity) {
    json.numIncrBy(getKey(entity), Path2.of("." + field.getSearchAlias()), value);
  }

}
