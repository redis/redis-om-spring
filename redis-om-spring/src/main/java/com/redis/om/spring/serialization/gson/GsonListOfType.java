package com.redis.om.spring.serialization.gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * A utility class that implements ParameterizedType to represent a List type with a specific
 * generic parameter. This is useful for Gson when deserializing JSON to typed List objects.
 * 
 * @param <T> the type of elements contained in the List
 */
public class GsonListOfType<T> implements ParameterizedType {

  private final Class<T> containedType;

  /**
   * Constructs a new GsonListOfType for the specified contained type.
   * 
   * @param containedType the class type of elements that the List will contain
   */
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
