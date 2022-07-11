package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;

import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import com.redislabs.modules.rejson.Path;

public class ArrayTrimAction<E> implements TakesJSONOperations, Consumer<E> {
  
  private Field field;
  private JSONOperations<String> json;
  private Long begin;
  private Long end;

  public ArrayTrimAction(Field field, Long begin, Long end) {
    this.field = field;
    this.begin = begin;
    this.end = end;
  }

  @Override
  public void setJSONOperations(JSONOperations<String> json) {
    this.json = json;
  }

  @Override
  public void accept(E entity) {
    Optional<?> maybeId = ObjectUtils.getIdFieldForEntity(entity);
    
    if (maybeId.isPresent()) {
      Optional<Class<?>> maybeClass = ObjectUtils.getCollectionElementType(field);
      if (maybeClass.isPresent()) {
        json.arrTrim(entity.getClass().getName() + ":" + maybeId.get().toString(), Path.of("." + field.getName()), begin, end); 
      } else {
        throw new RuntimeException("Cannot determine contain element type for collection " + field.getName());
      }
    } else {
      throw new IllegalArgumentException(entity.getClass().getName() + " does not appear to have an ID field");
    }
    
  }
}
