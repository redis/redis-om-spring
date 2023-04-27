package com.redis.om.spring.search.stream.actions;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import redis.clients.jedis.json.Path;

import java.util.function.ToLongFunction;

public class ArrayIndexOfAction<E> extends BaseAbstractAction implements ToLongFunction<E> {

  private final Object element;

  public ArrayIndexOfAction(SearchFieldAccessor field, Object element) {
    super(field);
    this.element = element;
  }

  @Override
  public long applyAsLong(E value) {
    return json.arrIndex(getKey(value), Path.of("." + field.getSearchAlias()), element);
  }
}
