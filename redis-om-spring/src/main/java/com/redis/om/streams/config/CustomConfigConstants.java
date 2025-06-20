package com.redis.om.streams.config;

/**
 * Constants used for Redis Streams configuration.
 * This class provides property names used for configuring Redis Streams behavior.
 */
public class CustomConfigConstants {
  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private CustomConfigConstants() {
  }

  /**
   * Property name for the maximum length of Redis streams.
   * Defines the maximum number of entries a stream can hold.
   */
  public static final String REDIS_STREAMS_MAX_LENGTH = "redis.streams.max.length";

  /**
   * Property name for the maximum allowed streams.
   * Defines the maximum number of streams allowed in the system.
   */
  public static final String REDIS_STREAMS_MAX_ALLOWED = "redis.streams.max.allowed";

  /**
   * Property name for the pending message count.
   * Defines the number of pending messages to retrieve when checking pending entries.
   */
  public static final String REDIS_STREAMS_PENDING_MSG_COUNT = "redis.streams.pending.msg.count";

  // Consumer Properties

  /**
   * Property name for the consumer read count.
   * Defines how many entries to read at once when consuming from a stream.
   */
  public static final String REDIS_STREAMS_CONSUMER_READ_COUNT = "redis.streams.consumer.read.count";

  /**
   * Property name for the consumer block count.
   * Defines how long (in milliseconds) to block when reading from a stream if no entries are available.
   */
  public static final String REDIS_STREAMS_CONSUMER_BLOCK_COUNT = "redis.streams.consumer.block.count";

  /**
   * Property name for the consumer read acknowledgment setting.
   * Defines whether entries should be automatically acknowledged when read by a consumer.
   */
  public static final String REDIS_STREAMS_CONSUMER_READ_ACK = "redis.streams.consumer.read.ack";

}
