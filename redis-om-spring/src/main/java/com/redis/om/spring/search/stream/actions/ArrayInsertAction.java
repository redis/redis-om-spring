package com.redis.om.spring.search.stream.actions;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

public class ArrayInsertAction<E> extends BaseAbstractAction implements Consumer<E> {

  private final Object value;
  private final Integer index;

  public ArrayInsertAction(SearchFieldAccessor field, Object value, Integer index) {
    super(field);
    this.value = value;
    this.index = index;
  }

  @Override
  public void accept(E entity) {
    json.arrInsert(getKey(entity), Path2.of("." + field.getSearchAlias()), index, value);
  }

}
