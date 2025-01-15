package com.redis.om.spring.mapping;

import jakarta.persistence.IdClass;
import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.redis.core.TimeToLiveAccessor;
import org.springframework.data.redis.core.mapping.BasicRedisPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisPersistentProperty;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RedisEnhancedPersistentEntity<T> extends BasicRedisPersistentEntity<T>
    implements RedisPersistentEntity<T> {

  private final List<RedisPersistentProperty> idProperties = new ArrayList<>();
  private final boolean hasIdClass;

  public RedisEnhancedPersistentEntity(TypeInformation<T> information, @Nullable KeySpaceResolver keySpaceResolver,
      TimeToLiveAccessor timeToLiveAccessor) {
    super(information, keySpaceResolver, timeToLiveAccessor);
    this.hasIdClass = information.getType().isAnnotationPresent(IdClass.class);
  }

  @Override
  public void addPersistentProperty(RedisPersistentProperty property) {
    if (property.isIdProperty()) {
      if (hasIdClass) {
        // For @IdClass, collect all @Id properties
        idProperties.add(property);
      } else {
        // Without @IdClass, enforce single @Id
        RedisPersistentProperty existingIdProperty = getIdProperty();
        if (existingIdProperty != null) {
          throw new MappingException(String.format(
              "Attempt to add explicit id property %s but already have a property %s registered as explicit id; Check your mapping configuration",
              property.getField(), existingIdProperty.getField()));
        }
      }
    }
    super.addPersistentProperty(property);
  }

  @Override
  protected RedisPersistentProperty returnPropertyIfBetterIdPropertyCandidateOrNull(RedisPersistentProperty property) {

    if (!property.isIdProperty()) {
      return null;
    }

    // For @IdClass, always accept new ID properties
    if (hasIdClass) {
      return property;
    }

    // For non-@IdClass, use parent's single ID logic
    return super.returnPropertyIfBetterIdPropertyCandidateOrNull(property);
  }

  @Override
  @Nullable
  public RedisPersistentProperty getIdProperty() {
    // For composite keys with @IdClass, return first ID property
    // For single ID, maintain original behavior
    return idProperties.isEmpty() ? super.getIdProperty() : idProperties.get(0);
  }

  /**
   * Returns all ID properties when using composite keys with @IdClass.
   * For single ID entities, returns a list with one element.
   */
  public List<RedisPersistentProperty> getIdProperties() {
    return idProperties;
  }

  /**
   * Checks if this entity uses composite ID via @IdClass
   */
  public boolean isIdClassComposite() {
    return hasIdClass;
  }
}
