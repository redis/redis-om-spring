package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchFieldAccessor {
  private final List<Field> fields = new ArrayList<>();
  private final String searchAlias;
  private final Class<?> targetClass;
  private final Class<?> declaringClass;

  public SearchFieldAccessor(String searchAlias, Field... fields) {
    this.searchAlias = searchAlias;
    this.fields.addAll(Arrays.asList(fields));
    this.targetClass = this.fields.get(0).getType();
    this.declaringClass = this.fields.get(0).getDeclaringClass();
  }

  public Field getField() {
    return fields.get(0);
  }

  public String getSearchAlias() {
    return searchAlias;
  }

  public Class<?> getTargetClass() {
    return targetClass;
  }

  public Class<?> getDeclaringClass() {
    return declaringClass;
  }
}
