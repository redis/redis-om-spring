package com.redis.om.spring.serialization.gson;

import org.springframework.data.annotation.Reference;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * A Gson exclusion strategy that prevents serialization of fields annotated with @Reference.
 * This strategy is used to exclude reference fields from JSON serialization to avoid
 * circular references and unnecessary data inclusion.
 */
public final class GsonReferencesSerializationExclusionStrategy implements ExclusionStrategy {

  /**
   * Singleton instance of the exclusion strategy.
   */
  public static final GsonReferencesSerializationExclusionStrategy INSTANCE = new GsonReferencesSerializationExclusionStrategy();

  /**
   * Private constructor to enforce singleton pattern.
   */
  private GsonReferencesSerializationExclusionStrategy() {
  }

  @Override
  public boolean shouldSkipField(FieldAttributes f) {
    return f.getAnnotation(Reference.class) != null;
  }

  @Override
  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }
}
