package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.function.ToLongFunction;

import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import com.redislabs.modules.rejson.Path;

public class ArrayIndexOfAction<E> implements TakesJSONOperations, ToLongFunction<E> {
  
  private Field field;
  private JSONOperations<String> json;
  private Object element;

  public ArrayIndexOfAction(Field field, Object element) {
    this.field = field;
    this.element = element;
  }

  @Override
  public void setJSONOperations(JSONOperations<String> json) {
    this.json = json;
  }

  @Override
  public long applyAsLong(E value) {
    String key = field.getDeclaringClass().getName() + ":" + ObjectUtils.getIdFieldForEntity(value).get().toString();
    return json.arrIndex(key, Path.of("." + field.getName()), element);
  }
}
