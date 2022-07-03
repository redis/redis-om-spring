package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.function.ToLongFunction;

import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import com.redislabs.modules.rejson.Path;

public class ArrayLengthAction<E> implements TakesJSONOperations, ToLongFunction<E> {

  private Field field;
  private JSONOperations<String> json;

  public ArrayLengthAction(Field field) {
    this.field = field;
  }

  @Override
  public long applyAsLong(E value) {
    String key = field.getDeclaringClass().getName() + ":" + ObjectUtils.getIdFieldForEntity(value).get().toString();
    return json.arrLen(key, Path.of("." + field.getName()));
  }
  
  @Override
  public void setJSONOperations(JSONOperations<String> json) {
    this.json = json;
  }

}
