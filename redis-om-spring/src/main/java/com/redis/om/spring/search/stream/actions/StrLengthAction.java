package com.redis.om.spring.search.stream.actions;

import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

public class StrLengthAction<E> extends BaseAbstractAction implements ToLongFunction<E> {

  public StrLengthAction(SearchFieldAccessor field) {
    super(field);
  }

  @Override
  public long applyAsLong(E value) {
    var result = json.strLen(getKey(value), Path2.of("." + field.getSearchAlias()));
    return result != null && !result.isEmpty() ? result.get(0) : 0;
  }

}
