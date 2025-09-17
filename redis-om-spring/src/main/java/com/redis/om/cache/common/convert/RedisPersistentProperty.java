package com.redis.om.cache.common.convert;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentProperty;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

/**
 * Redis specific {@link PersistentProperty} implementation.
 *
 */
public class RedisPersistentProperty extends KeyValuePersistentProperty<RedisPersistentProperty> {

  private static final Set<String> SUPPORTED_ID_PROPERTY_NAMES = new HashSet<>();

  static {
    SUPPORTED_ID_PROPERTY_NAMES.add("id");
  }

  /**
   * Creates new {@link RedisPersistentProperty}.
   *
   * @param property         the property to be persisted
   * @param owner            the entity owning the property
   * @param simpleTypeHolder holder of simple type information
   */
  public RedisPersistentProperty(Property property, PersistentEntity<?, RedisPersistentProperty> owner,
      SimpleTypeHolder simpleTypeHolder) {
    super(property, owner, simpleTypeHolder);
  }

  @Override
  public boolean isIdProperty() {

    if (super.isIdProperty()) {
      return true;
    }

    return SUPPORTED_ID_PROPERTY_NAMES.contains(getName());
  }
}
