package com.redis.om.streams.config;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.redis.om.streams.annotation.EnableRedisStreams;

/**
 * Condition that determines whether Redis Streams functionality should be enabled.
 * 
 * <p>This condition is used by {@link RedisStreamsConfiguration} to determine whether
 * the Redis Streams infrastructure should be initialized. The condition checks for
 * either of the following:</p>
 * 
 * <ul>
 * <li>The property {@code redis.streams.enabled} is set to {@code true}</li>
 * <li>The {@link EnableRedisStreams} annotation is present on any class in the application</li>
 * </ul>
 * 
 * <p>If either condition is met, Redis Streams functionality will be enabled.</p>
 * 
 * @see RedisStreamsConfiguration
 * @see EnableRedisStreams
 */
public class RedisStreamsCondition extends SpringBootCondition {

  /** Property name to check for Redis Streams enablement */
  private static final String PROPERTY_NAME = "redis.streams.enabled";

  /**
   * Determines whether Redis Streams functionality should be enabled.
   * 
   * <p>This method checks the following conditions in order:</p>
   * <ol>
   * <li>If the property {@code redis.streams.enabled} is explicitly set to {@code false},
   * Redis Streams will not be enabled</li>
   * <li>If the property {@code redis.streams.enabled} is explicitly set to {@code true},
   * Redis Streams will be enabled</li>
   * <li>If the {@link EnableRedisStreams} annotation is present on any class,
   * Redis Streams will be enabled</li>
   * <li>Otherwise, Redis Streams will not be enabled</li>
   * </ol>
   * 
   * @param context  the condition context
   * @param metadata the metadata of the annotated element
   * @return the condition outcome indicating whether Redis Streams should be enabled
   */
  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    ConditionMessage.Builder message = ConditionMessage.forCondition("Redis Streams");

    // Check if the property is explicitly set to false
    String propertyValue = context.getEnvironment().getProperty(PROPERTY_NAME);
    if (propertyValue != null && "false".equalsIgnoreCase(propertyValue)) {
      return ConditionOutcome.noMatch(message.because("Property '" + PROPERTY_NAME + "' is explicitly set to false"));
    }

    // Check if the property is explicitly set to true
    if (propertyValue != null && "true".equalsIgnoreCase(propertyValue)) {
      return ConditionOutcome.match(message.because("Property '" + PROPERTY_NAME + "' is set to true"));
    }

    // Check if EnableRedisStreams annotation is present on any class
    if (hasEnableRedisStreamsAnnotation(context)) {
      return ConditionOutcome.match(message.because("EnableRedisStreams annotation is present"));
    }

    return ConditionOutcome.noMatch(message.because(
        "Neither property '" + PROPERTY_NAME + "' is true nor EnableRedisStreams annotation is present"));
  }

  /**
   * Checks if the {@link EnableRedisStreams} annotation is present on any bean in the application.
   * 
   * <p>This method scans all bean definitions in the application context and checks
   * if any of them have the {@link EnableRedisStreams} annotation.</p>
   * 
   * @param context the condition context
   * @return true if the annotation is present on any bean, false otherwise
   */
  private boolean hasEnableRedisStreamsAnnotation(ConditionContext context) {
    try {
      // Get all bean definition names
      String[] beanNames = context.getBeanFactory().getBeanDefinitionNames();
      for (String beanName : beanNames) {
        try {
          Class<?> beanClass = context.getBeanFactory().getType(beanName);
          if (beanClass != null && beanClass.isAnnotationPresent(EnableRedisStreams.class)) {
            return true;
          }
        } catch (Exception e) {
          // Ignore exceptions when getting bean type
        }
      }
    } catch (Exception e) {
      // If we can't determine, assume it's not enabled
    }
    return false;
  }
}
