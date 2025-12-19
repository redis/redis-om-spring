package com.redis.om.cache.common.convert;

import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.core.mapping.BasicKeyValuePersistentEntity;
import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link RedisPersistentEntity} implementation.
 *
 * @param <T> the type of the entity
 */
public class BasicRedisPersistentEntity<T> extends BasicKeyValuePersistentEntity<T, RedisPersistentProperty> implements
    RedisPersistentEntity<T> {

  /**
   * Creates new {@link BasicRedisPersistentEntity}.
   *
   * @param information      must not be {@literal null}.
   * @param keySpaceResolver can be {@literal null}.
   */
  public BasicRedisPersistentEntity(TypeInformation<T> information, @Nullable KeySpaceResolver keySpaceResolver) {
    super(information, keySpaceResolver);

  }

  @Override
  @Nullable
  protected RedisPersistentProperty returnPropertyIfBetterIdPropertyCandidateOrNull(RedisPersistentProperty property) {

    Assert.notNull(property, "Property must not be null");

    if (!property.isIdProperty()) {
      return null;
    }

    RedisPersistentProperty currentIdProperty = getIdProperty();
    boolean currentIdPropertyIsSet = currentIdProperty != null;

    if (!currentIdPropertyIsSet) {
      return property;
    }

    boolean currentIdPropertyIsExplicit = currentIdProperty.isAnnotationPresent(Id.class);
    boolean newIdPropertyIsExplicit = property.isAnnotationPresent(Id.class);

    if (currentIdPropertyIsExplicit && newIdPropertyIsExplicit) {
      throw new MappingException(String.format(
          "Attempt to add explicit id property %s but already have an property %s registered " + "as explicit id; Check your mapping configuration",
          property.getField(), currentIdProperty.getField()));
    }

    if (!currentIdPropertyIsExplicit && !newIdPropertyIsExplicit) {
      throw new MappingException(String.format(
          "Attempt to add id property %s but already have an property %s registered " + "as id; Check your mapping configuration",
          property.getField(), currentIdProperty.getField()));
    }

    if (newIdPropertyIsExplicit) {
      return property;
    }

    return null;
  }
}
