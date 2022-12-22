package com.redis.om.spring.search.stream.actions;

import java.util.function.Consumer;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import redis.clients.jedis.json.Path;

public class ArrayAppendAction<E> extends BaseAbstractAction implements Consumer<E> {

  private final Object value;

  public ArrayAppendAction(SearchFieldAccessor field, Object value) {
    super(field);
    this.value = value;
  }

  @Override
  public void accept(E entity) {
    json.arrAppend(getKey(entity), Path.of("." + field.getSearchAlias()), value);
  }

}
