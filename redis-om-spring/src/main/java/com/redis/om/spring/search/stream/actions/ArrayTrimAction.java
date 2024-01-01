package com.redis.om.spring.search.stream.actions;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import redis.clients.jedis.json.Path2;

import java.util.function.Consumer;

public class ArrayTrimAction<E> extends BaseAbstractAction implements Consumer<E> {

  private final Integer begin;
  private final Integer end;

  public ArrayTrimAction(SearchFieldAccessor field, Integer begin, Integer end) {
    super(field);
    this.begin = begin;
    this.end = end;
  }

  @Override
  public void accept(E entity) {
    json.arrTrim(getKey(entity), Path2.of("." + field.getSearchAlias()), begin, end);
  }
}
