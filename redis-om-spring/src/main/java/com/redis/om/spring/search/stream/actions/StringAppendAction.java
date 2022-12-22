package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import redis.clients.jedis.json.Path;

public class StringAppendAction<E> extends BaseAbstractAction implements Consumer<E> {

  private String value;

  public StringAppendAction(Field field, String value) {
    super(field);
    this.value = value;
  }

  @Override
  public void accept(E entity) {
    json.strAppend(getKey(entity), Path.of("." + field.getName()), value);
  }

}
