package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import com.redislabs.modules.rejson.Path;

public class ArrayPopAction<E,R> implements TakesJSONOperations, Function<E,R> {
  
  private Field field;
  private JSONOperations<String> json;
  private Long index;

  public ArrayPopAction(Field field, Long index) {
    this.field = field;
    this.index = index;
  }

  @Override
  public void setJSONOperations(JSONOperations<String> json) {
    this.json = json;
  }

  @SuppressWarnings("unchecked")
  @Override
  public R apply(E entity) {
    Optional<?> maybeId = ObjectUtils.getIdFieldForEntity(entity);
    
    if (maybeId.isPresent()) {
      Optional<Class<?>> maybeClass = ObjectUtils.getCollectionElementType(field);
      if (maybeClass.isPresent()) {
        return (R) json.arrPop(entity.getClass().getName() + ":" + maybeId.get().toString(), maybeClass.get(), Path.of("." + field.getName()), index); 
      } else {
        throw new RuntimeException("Cannot determine contain element type for collection " + field.getName());
      }
    } else {
      throw new IllegalArgumentException(entity.getClass().getName() + " does not appear to have an ID field");
    }
  }
}
