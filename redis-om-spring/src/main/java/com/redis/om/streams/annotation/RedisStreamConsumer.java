package com.redis.om.streams.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a Redis Stream consumer.
 * Classes annotated with this will be registered as consumers for the specified Redis Stream.
 */
@Target(
  ElementType.TYPE
)
@Retention(
  RetentionPolicy.RUNTIME
)
public @interface RedisStreamConsumer {
  /**
   * The name of the Redis Stream topic to consume messages from.
   * 
   * @return the topic name
   */
  String topicName();

  /**
   * The name of the consumer group this consumer belongs to.
   * Consumer groups allow multiple consumers to cooperatively consume messages from a stream.
   * 
   * @return the consumer group name
   */
  String groupName();

  /**
   * The name of the consumer within the consumer group.
   * If not specified, a default name will be generated.
   * 
   * @return the consumer name
   */
  String consumerName() default "";

  /**
   * Whether to automatically acknowledge messages after processing.
   * If set to false, messages must be explicitly acknowledged.
   * 
   * @return true if messages should be auto-acknowledged, false otherwise
   */
  boolean autoAck() default false;

  /**
   * Whether the Redis deployment is a cluster.
   * This affects how the consumer interacts with Redis.
   * 
   * @return true if Redis is deployed as a cluster, false otherwise
   */
  boolean cluster() default false;
}
