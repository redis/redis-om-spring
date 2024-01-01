package com.redis.om.spring.search.stream.actions;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import redis.clients.jedis.json.Path2;

import java.util.function.Consumer;

public class ArrayAppendAction<E> extends BaseAbstractAction implements Consumer<E> {

  private final Object value;

  public ArrayAppendAction(SearchFieldAccessor field, Object value) {
    super(field);
    this.value = value;
  }

  @Override
  public void accept(E entity) {
    json.arrAppend(getKey(entity), Path2.of("." + field.getSearchAlias()), value);
  }

}
