package com.redis.om.spring.search.stream.actions;

import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

public class ArrayIndexOfAction<E> extends BaseAbstractAction implements ToLongFunction<E> {

  private final Object element;

  public ArrayIndexOfAction(SearchFieldAccessor field, Object element) {
    super(field);
    this.element = element;
  }

  @Override
  public long applyAsLong(E value) {
    var result = json.arrIndex(getKey(value), Path2.of("." + field.getSearchAlias()), element);
    return result != null && !result.isEmpty() ? result.get(0) : 0;
  }
}
