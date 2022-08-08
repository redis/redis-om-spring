package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;

import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;

public abstract class BaseAbstractAction implements TakesJSONOperations {
  protected Field field;
  protected JSONOperations<String> json;
  private Field idField;
  
  public BaseAbstractAction(Field field) {
    this.field = field;
    Optional<Field> maybeId = ObjectUtils.getIdFieldForEntityClass(field.getDeclaringClass());
    if (maybeId.isPresent()) {
      this.idField = maybeId.get();
    } else {
      throw new IllegalArgumentException(field.getDeclaringClass().getName() + " does not appear to have an ID field");
    }
  }

  @Override
  public void setJSONOperations(JSONOperations<String> json) {
    this.json = json;
  }
  
  protected String getKey(Object entity) {
    String id = ObjectUtils.getIdFieldForEntity(idField, entity).toString();    
    return field.getDeclaringClass().getName() + ":" + id;
  }
}
