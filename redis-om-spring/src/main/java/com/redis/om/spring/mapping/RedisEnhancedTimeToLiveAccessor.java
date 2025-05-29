package com.redis.om.spring.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.TimeToLiveAccessor;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration.KeyspaceSettings;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.*;

/**
 * Enhanced Time-To-Live (TTL) accessor implementation for Redis OM Spring that provides
 * comprehensive TTL management capabilities for Redis entities.
 * 
 * <p>This implementation extends Spring Data Redis's TTL functionality by integrating
 * with Redis OM Spring's {@link KeyspaceConfiguration} and {@link RedisEnhancedMappingContext}
 * to provide flexible TTL resolution strategies. It supports multiple TTL configuration
 * approaches including annotation-based, property-based, and method-based TTL definitions.
 * 
 * <p>The TTL resolution follows this priority order:
 * <ol>
 * <li>Property-level {@code @TimeToLive} annotations on entity fields</li>
 * <li>Method-level {@code @TimeToLive} annotations on entity methods</li>
 * <li>Class-level {@code @RedisHash(timeToLive = ...)} configurations</li>
 * <li>KeyspaceConfiguration default timeouts</li>
 * </ol>
 * 
 * <p>This accessor is particularly useful in Redis environments where different entity
 * types require different expiration strategies, and supports both static and dynamic
 * TTL values based on entity state.
 * 
 * <p>Key features:
 * <ul>
 * <li>Caches resolved TTL properties and methods for performance</li>
 * <li>Supports multiple time units (seconds, minutes, hours, etc.)</li>
 * <li>Handles partial updates through {@link PartialUpdate} objects</li>
 * <li>Integrates with Redis OM Spring's enhanced mapping context</li>
 * </ul>
 * 
 * @author Redis OM Spring Team
 * @since 1.0.0
 * @see TimeToLiveAccessor
 * @see KeyspaceConfiguration
 * @see RedisEnhancedMappingContext
 * @see TimeToLive
 */
public class RedisEnhancedTimeToLiveAccessor implements TimeToLiveAccessor {

  private final Map<Class<?>, Long> defaultTimeouts = new HashMap<>();
  private final Map<Class<?>, PersistentProperty<?>> timeoutProperties = new HashMap<>();
  private final Map<Class<?>, Method> timeoutMethods = new HashMap<>();
  private final KeyspaceConfiguration keyspaceConfig;
  private final RedisEnhancedMappingContext mappingContext;

  /**
   * Constructs a new RedisEnhancedTimeToLiveAccessor with the specified configuration
   * and mapping context.
   * 
   * <p>This constructor initializes the TTL accessor with the necessary components
   * for resolving TTL values from various sources including keyspace configuration,
   * entity annotations, and dynamic property values.
   * 
   * <p>The accessor will use the provided keyspace configuration to determine
   * default TTL values for entity types, and the mapping context to access
   * entity metadata and property values during TTL resolution.
   * 
   * @param keyspaceConfig the keyspace configuration containing default TTL settings
   *                       for different entity types, must not be {@literal null}
   * @param mappingContext the Redis enhanced mapping context for accessing entity
   *                       metadata and property values, must not be {@literal null}
   * @throws IllegalArgumentException if either parameter is {@literal null}
   */
  public RedisEnhancedTimeToLiveAccessor(KeyspaceConfiguration keyspaceConfig,
      RedisEnhancedMappingContext mappingContext) {
    Assert.notNull(keyspaceConfig, "KeyspaceConfiguration must not be null");
    Assert.notNull(mappingContext, "MappingContext must not be null");

    this.keyspaceConfig = keyspaceConfig;
    this.mappingContext = mappingContext;
  }

  @Override
  @SuppressWarnings(
    { "rawtypes" }
  )
  public Long getTimeToLive(Object source) {
    Assert.notNull(source, "Source must not be null");
    Class<?> type = source instanceof Class<?> ?
        (Class<?>) source :
        (source instanceof PartialUpdate ? ((PartialUpdate) source).getTarget() : source.getClass());

    Long defaultTimeout = resolveDefaultTimeOut(type);
    TimeUnit unit = TimeUnit.SECONDS;

    PersistentProperty<?> ttlProperty = resolveTtlProperty(type);

    if (ttlProperty != null && ttlProperty.isAnnotationPresent(TimeToLive.class)) {
      unit = ttlProperty.getRequiredAnnotation(TimeToLive.class).unit();
    }

    if (source instanceof PartialUpdate<?> update) {
      if (ttlProperty != null && !update.getPropertyUpdates().isEmpty()) {
        for (PartialUpdate.PropertyUpdate pUpdate : update.getPropertyUpdates()) {
          if (PartialUpdate.UpdateCommand.SET.equals(pUpdate.getCmd()) && ttlProperty.getName().equals(pUpdate
              .getPropertyPath())) {
            return TimeUnit.SECONDS.convert(NumberUtils.convertNumberToTargetClass((Number) pUpdate.getValue(),
                Long.class), unit);
          }
        }
      }
    } else if (ttlProperty != null) {
      RedisPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(type);
      Object ttlPropertyValue = entity.getPropertyAccessor(source).getProperty(ttlProperty);
      if (ttlPropertyValue != null) {
        return TimeUnit.SECONDS.convert(((Number) ttlPropertyValue).longValue(), unit);
      }
    } else {
      Method timeoutMethod = resolveTimeMethod(type);
      if (timeoutMethod != null) {
        ReflectionUtils.makeAccessible(timeoutMethod);
        TimeToLive ttl = AnnotationUtils.findAnnotation(timeoutMethod, TimeToLive.class);
        try {
          Number timeout = (Number) timeoutMethod.invoke(source);
          if (timeout != null && ttl != null) {
            return TimeUnit.SECONDS.convert(timeout.longValue(), ttl.unit());
          }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
          throw new IllegalStateException(String.format("Cannot access method '%s': %s", timeoutMethod.getName(), ex
              .getMessage()), ex);
        }
      }
    }

    return defaultTimeout;
  }

  @Override
  public boolean isExpiringEntity(Class<?> type) {
    Long defaultTimeOut = resolveDefaultTimeOut(type);

    if (defaultTimeOut != null && defaultTimeOut > 0) {
      return true;
    }

    if (resolveTtlProperty(type) != null) {
      return true;
    }

    return resolveTimeMethod(type) != null;
  }

  @Nullable
  private Long resolveDefaultTimeOut(Class<?> type) {
    if (this.defaultTimeouts.containsKey(type)) {
      return defaultTimeouts.get(type);
    }

    Long defaultTimeout = null;

    if (keyspaceConfig.hasSettingsFor(type)) {
      defaultTimeout = keyspaceConfig.getKeyspaceSettings(type).getTimeToLive();
    }

    RedisHash hash = mappingContext.getRequiredPersistentEntity(type).findAnnotation(RedisHash.class);
    if (hash != null && hash.timeToLive() > 0) {
      defaultTimeout = hash.timeToLive();
    }

    defaultTimeouts.put(type, defaultTimeout);
    return defaultTimeout;
  }

  @Nullable
  private PersistentProperty<?> resolveTtlProperty(Class<?> type) {
    if (timeoutProperties.containsKey(type)) {
      return timeoutProperties.get(type);
    }

    RedisPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(type);
    PersistentProperty<?> ttlProperty = entity.getPersistentProperty(TimeToLive.class);

    if (ttlProperty != null) {
      timeoutProperties.put(type, ttlProperty);
      return ttlProperty;
    }

    if (keyspaceConfig.hasSettingsFor(type)) {
      KeyspaceSettings settings = keyspaceConfig.getKeyspaceSettings(type);
      if (StringUtils.hasText(settings.getTimeToLivePropertyName())) {
        ttlProperty = entity.getPersistentProperty(settings.getTimeToLivePropertyName());
        if (ttlProperty != null) {
          timeoutProperties.put(type, ttlProperty);
          return ttlProperty;
        }
      }
    }

    timeoutProperties.put(type, null);
    return null;
  }

  @Nullable
  private Method resolveTimeMethod(Class<?> type) {
    if (timeoutMethods.containsKey(type)) {
      return timeoutMethods.get(type);
    }

    timeoutMethods.put(type, null);
    ReflectionUtils.doWithMethods(type, method -> timeoutMethods.put(type, method), method -> ClassUtils.isAssignable(
        Number.class, method.getReturnType()) && AnnotationUtils.findAnnotation(method, TimeToLive.class) != null);

    return timeoutMethods.get(type);
  }
}