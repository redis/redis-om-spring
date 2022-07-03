package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;

import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import com.redislabs.modules.rejson.Path;

public class ArrayInsertAction<E> implements TakesJSONOperations, Consumer<E> {
  
  private Field field;
  private JSONOperations<String> json;
  private Object value;
  private Long index;

  public ArrayInsertAction(Field field, Object value, Long index) {
    this.field = field;
    this.value = value;
    this.index = index;
  }

  @Override
  public void accept(E entity) {
    Optional<?> maybeId = ObjectUtils.getIdFieldForEntity(entity);
    
    if (maybeId.isPresent()) {
      json.arrInsert(entity.getClass().getName() + ":" + maybeId.get().toString(), Path.of("." + field.getName()), index, value);
    }
  }

  @Override
  public void setJSONOperations(JSONOperations<String> json) {
    this.json = json;
  }
}
