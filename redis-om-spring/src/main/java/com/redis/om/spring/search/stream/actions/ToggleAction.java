package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;

import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import com.redislabs.modules.rejson.Path;

public class ToggleAction<E> implements Consumer<E> {
  
  private Field field;
  private JSONOperations<String> json;

  public ToggleAction(Field field) {
    this.field = field;
  }

  @Override
  public void accept(E entity) {
    Optional<?> maybeId = ObjectUtils.getIdFieldForEntity(entity);
    
    if (maybeId.isPresent()) {
      json.toggle(entity.getClass().getName() + ":" + maybeId.get().toString(), Path.of("." + field.getName()));
    }
  }

  public void setJSONOperations(JSONOperations<String> json) {
    this.json = json;
  }
}
