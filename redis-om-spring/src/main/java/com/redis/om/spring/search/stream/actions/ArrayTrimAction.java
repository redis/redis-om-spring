package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redislabs.modules.rejson.Path;

public class ArrayTrimAction<E> extends BaseAbstractAction implements Consumer<E> {

  private Long begin;
  private Long end;

  public ArrayTrimAction(SearchFieldAccessor field, Long begin, Long end) {
    super(field);
    this.begin = begin;
    this.end = end;
  }

  @Override
  public void accept(E entity) {
    json.arrTrim(getKey(entity), Path.of("." + field.getSearchAlias()), begin, end);
  }
}
