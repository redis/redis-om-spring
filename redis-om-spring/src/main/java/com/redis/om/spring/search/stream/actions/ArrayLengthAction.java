package com.redis.om.spring.search.stream.actions;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import redis.clients.jedis.json.Path2;

import java.util.function.ToLongFunction;

public class ArrayLengthAction<E> extends BaseAbstractAction implements ToLongFunction<E> {

  public ArrayLengthAction(SearchFieldAccessor field) {
    super(field);
  }

  @Override
  public long applyAsLong(E value) {
    var result = json.arrLen(getKey(value), Path2.of("." + field.getSearchAlias()));
    return result != null && !result.isEmpty() ? result.get(0) : 0;
  }

}
