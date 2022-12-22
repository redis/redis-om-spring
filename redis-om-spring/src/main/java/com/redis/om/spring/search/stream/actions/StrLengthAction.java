package com.redis.om.spring.search.stream.actions;

import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import redis.clients.jedis.json.Path;

public class StrLengthAction<E> extends BaseAbstractAction implements ToLongFunction<E> {

  public StrLengthAction(SearchFieldAccessor field) {
    super(field);
  }

  @Override
  public long applyAsLong(E value) {
    return json.strLen(getKey(value), Path.of("." + field.getSearchAlias()));
  }

}
