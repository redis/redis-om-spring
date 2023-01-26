package com.redis.om.spring.search.stream.actions;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import redis.clients.jedis.json.Path;

public class ToggleAction<E> extends BaseAbstractAction implements Consumer<E> {
  public ToggleAction(SearchFieldAccessor field) {
    super(field);
  }

  @Override
  public void accept(E entity) {
    json.toggle(getKey(entity), Path.of("." + field.getSearchAlias()));
  }
}
