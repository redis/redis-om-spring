package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import redis.clients.jedis.json.Path;

public class ArrayTrimAction<E> extends BaseAbstractAction implements Consumer<E> {

  private int begin;
  private int end;

  public ArrayTrimAction(Field field, int begin, int end) {
    super(field);
    this.begin = begin;
    this.end = end;
  }

  @Override
  public void accept(E entity) {
    json.arrTrim(getKey(entity), Path.of("." + field.getName()), begin, end);
  }
}
