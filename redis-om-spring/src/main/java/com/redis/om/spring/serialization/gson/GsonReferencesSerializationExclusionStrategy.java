package com.redis.om.spring.serialization.gson;

import org.springframework.data.annotation.Reference;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public final class GsonReferencesSerializationExclusionStrategy implements ExclusionStrategy {

  public static final GsonReferencesSerializationExclusionStrategy INSTANCE = new GsonReferencesSerializationExclusionStrategy();

  @Override
  public boolean shouldSkipField(FieldAttributes f) {
    return f.getAnnotation(Reference.class) != null;
  }

  @Override
  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }
}
