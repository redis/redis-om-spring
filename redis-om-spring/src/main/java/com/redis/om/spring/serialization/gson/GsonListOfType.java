package com.redis.om.spring.serialization.gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class GsonListOfType<T> implements ParameterizedType {

  private final Class<T> containedType;

  public GsonListOfType(Class<T> containedType) {
    this.containedType = containedType;
  }

  public Type[] getActualTypeArguments() {
    return new Type[] { containedType };
  }

  public Type getRawType() {
    return List.class;
  }

  public Type getOwnerType() {
    return null;
  }
}
