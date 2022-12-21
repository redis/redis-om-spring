package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;

public abstract class BaseAbstractAction implements TakesJSONOperations {
  protected SearchFieldAccessor field;
  protected JSONOperations<String> json;
  protected Field idField;

  protected BaseAbstractAction(SearchFieldAccessor field) {
    this.field = field;
    Class<?> entityClass = field.getDeclaringClass();
    Optional<Field> maybeId = ObjectUtils.getIdFieldForEntityClass(entityClass);
    if (maybeId.isPresent()) {
      this.idField = maybeId.get();
    } else {
      throw new NullPointerException(
          String.format("Entity Class %s does not have an ID field", entityClass.getSimpleName()));
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
