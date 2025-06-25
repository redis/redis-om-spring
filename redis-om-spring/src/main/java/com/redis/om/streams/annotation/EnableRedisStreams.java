package com.redis.om.streams.annotation;

import java.lang.annotation.*;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.redis.om.streams.config.RedisStreamConsumerRegistrar;

/**
 * Enables Redis Streams support in a Spring application.
 * This annotation should be applied to a Spring @Configuration class to scan for
 * and register Redis Stream consumers annotated with {@link RedisStreamConsumer}.
 * <p>
 * Example usage:
 * <pre>
 * &#64;Configuration
 * &#64;EnableRedisStreams(basePackages = "com.example.streams")
 * public class AppConfig {
 * // configuration
 * }
 * </pre>
 */
@Target(
  ElementType.TYPE
)
@Retention(
  RetentionPolicy.RUNTIME
)
@Documented
@Import(
  RedisStreamConsumerRegistrar.class
)
@EnableScheduling
public @interface EnableRedisStreams {

  /**
   * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
   * {@code @EnableRedisRepositories("org.my.pkg")} instead of
   * {@code @EnableRedisRepositories(basePackages="org.my.pkg")}.
   *
   * @return basePackages
   */
  @AliasFor(
    "basePackages"
  )
  String[] value() default {};

  /**
   * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
   * attribute.
   *
   * @return basePackages as a String
   */
  @AliasFor(
    "value"
  )
  String[] basePackages() default {};

}
