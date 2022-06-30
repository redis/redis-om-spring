package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;

import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import com.redislabs.modules.rejson.Path;

public class ArrayAppendAction<E> implements Consumer<E> {
  
  private Field field;
  private JSONOperations<String> json;
  private Object value;

  public ArrayAppendAction(Field field, Object value) {
    this.field = field;
    this.value = value;
  }

  @Override
  public void accept(E entity) {
    Optional<?> maybeId = ObjectUtils.getIdFieldForEntity(entity);
    
    if (maybeId.isPresent()) {
      json.arrAppend(entity.getClass().getName() + ":" + maybeId.get().toString(), Path.of("." + field.getName()), value);
    }
  }

  public void setJSONOperations(JSONOperations<String> json) {
    this.json = json;
  }
}
