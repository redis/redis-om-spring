package com.redis.om.spring.search.stream.actions;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import redis.clients.jedis.json.Path;

import java.util.function.Consumer;

public class ToggleAction<E> extends BaseAbstractAction implements Consumer<E> {
  public ToggleAction(SearchFieldAccessor field) {
    super(field);
  }

  @Override
  public void accept(E entity) {
    json.toggle(getKey(entity), Path.of("." + field.getSearchAlias()));
  }
}
