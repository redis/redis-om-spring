package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.function.ToLongFunction;

import redis.clients.jedis.json.Path;

public class StrLengthAction<E> extends BaseAbstractAction implements ToLongFunction<E> {

  public StrLengthAction(Field field) {
    super(field);
  }

  @Override
  public long applyAsLong(E value) {
    return json.strLen(getKey(value), Path.of("." + field.getName()));
  }

}
