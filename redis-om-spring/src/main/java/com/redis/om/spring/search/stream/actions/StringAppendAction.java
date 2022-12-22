package com.redis.om.spring.search.stream.actions;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import redis.clients.jedis.json.Path;

public class StringAppendAction<E> extends BaseAbstractAction implements Consumer<E> {

  private final String value;

  public StringAppendAction(SearchFieldAccessor field, String value) {
    super(field);
    this.value = value;
  }

  @Override
  public void accept(E entity) {
    json.strAppend(getKey(entity), Path.of("." + field.getSearchAlias()), value);
  }

}
