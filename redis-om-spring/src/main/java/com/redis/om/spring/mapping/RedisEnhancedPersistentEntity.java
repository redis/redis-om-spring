package com.redis.om.spring.mapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.redis.core.TimeToLiveAccessor;
import org.springframework.data.redis.core.mapping.BasicRedisPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisPersistentProperty;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;

import com.redis.om.spring.annotations.RedisKey;
import com.redis.om.spring.util.ObjectUtils;

import jakarta.persistence.IdClass;

/**
 * Enhanced Redis persistent entity that extends Spring Data Redis's {@link BasicRedisPersistentEntity}
 * to provide advanced mapping capabilities for Redis OM Spring.
 * 
 * <p>This persistent entity implementation adds support for:
 * <ul>
 * <li>Composite primary keys using JPA's {@code @IdClass} annotation</li>
 * <li>Multiple {@code @Id} properties within a single entity</li>
 * <li>Enhanced validation and error handling for ID property conflicts</li>
 * <li>Backward compatibility with standard single-field primary keys</li>
 * </ul>
 * 
 * <p>When an entity class is annotated with {@code @IdClass}, this implementation allows
 * multiple properties to be marked with {@code @Id}, collecting them as composite key components.
 * For entities without {@code @IdClass}, it enforces the standard Spring Data Redis behavior
 * of allowing only a single {@code @Id} property.
 * 
 * <p>This design enables Redis OM Spring to support complex entity relationships and
 * mapping scenarios while maintaining full compatibility with existing Spring Data Redis entities.
 * 
 * @param <T> the entity type
 * @see org.springframework.data.redis.core.mapping.BasicRedisPersistentEntity
 * @see org.springframework.data.redis.core.mapping.RedisPersistentEntity
 * @see jakarta.persistence.IdClass
 * @since 1.0
 */
public class RedisEnhancedPersistentEntity<T> extends BasicRedisPersistentEntity<T> implements
    RedisPersistentEntity<T> {

  private final List<RedisPersistentProperty> idProperties = new ArrayList<>();
  private final boolean hasIdClass;
  private volatile Optional<Field> redisKeyField = null; // Lazy initialized

  /**
   * Creates a new {@code RedisEnhancedPersistentEntity} for the given type information.
   * 
   * <p>This constructor initializes the persistent entity with enhanced support for composite keys.
   * It automatically detects if the entity class is annotated with {@code @IdClass} and adjusts
   * its behavior accordingly to support either single or composite primary keys.
   * 
   * @param information        the type information for the entity class; must not be {@literal null}
   * @param keySpaceResolver   the resolver for determining Redis keyspace; may be {@literal null}
   * @param timeToLiveAccessor the accessor for TTL configuration; must not be {@literal null}
   */
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
   * 
   * @return a list of all ID properties; never {@literal null} but may be empty
   */
  public List<RedisPersistentProperty> getIdProperties() {
    return idProperties;
  }

  /**
   * Checks if this entity uses composite ID via @IdClass.
   * 
   * @return {@literal true} if the entity class is annotated with {@code @IdClass},
   *         {@literal false} otherwise
   */
  public boolean isIdClassComposite() {
    return hasIdClass;
  }

  /**
   * Returns the field annotated with @RedisKey if present.
   * This method caches the result for performance.
   * 
   * @return an {@link Optional} containing the @RedisKey field if present,
   *         or an empty Optional if no field has the annotation
   */
  public Optional<Field> getRedisKeyField() {
    if (redisKeyField == null) {
      synchronized (this) {
        if (redisKeyField == null) {
          redisKeyField = findRedisKeyField();
        }
      }
    }
    return redisKeyField;
  }

  /**
   * Populates the @RedisKey field of the given entity with the provided Redis key.
   * This method uses the cached field information for optimal performance.
   * 
   * @param entity   the entity to populate
   * @param redisKey the Redis key to set
   * @param <E>      the entity type
   * @return the entity with populated @RedisKey field
   */
  @SuppressWarnings(
    "unchecked"
  )
  public <E> E populateRedisKey(E entity, String redisKey) {
    if (entity == null || redisKey == null) {
      return entity;
    }

    Optional<Field> fieldOpt = getRedisKeyField();
    if (fieldOpt.isPresent()) {
      try {
        fieldOpt.get().set(entity, redisKey);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Failed to set @RedisKey field", e);
      }
    }

    return entity;
  }

  private Optional<Field> findRedisKeyField() {
    List<Field> fields = ObjectUtils.getDeclaredFieldsTransitively(getType());
    for (Field field : fields) {
      if (field.isAnnotationPresent(RedisKey.class)) {
        field.setAccessible(true);
        return Optional.of(field);
      }
    }
    return Optional.empty();
  }
}
