package com.redis.om.streams.annotation;

import java.lang.annotation.*;

/**
 * Annotation to enable Redis Streams support in a Spring application.
 * 
 * <p>This annotation should be applied to a Spring {@code @Configuration} class to
 * enable the Redis Streams infrastructure. When present, the application will be able
 * to use Redis Streams for messaging and event processing.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * &#64;Configuration
 * &#64;EnableRedisStreams
 * public class AppConfig {
 * // configuration details
 * }
 * </pre>
 * 
 * @see com.redis.om.streams.config.RedisStreamsConfiguration
 * @since 1.0
 */
@Documented
@Target(
  { ElementType.TYPE }
)
@Retention(
  RetentionPolicy.RUNTIME
)
public @interface EnableRedisStreams {
}
