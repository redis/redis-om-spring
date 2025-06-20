package com.redis.om.streams.handler;

import com.redis.om.streams.command.ConsumerGroupBase;
import com.redis.om.streams.command.noack.NoAckConsumerGroup;
import com.redis.om.streams.command.serial.ConsumerGroup;
import com.redis.om.streams.command.serial.TopicManager;
import com.redis.om.streams.command.singleclusterpel.SingleClusterPelConsumerGroup;

/**
 * Abstract base class for Redis Streams message handlers.
 * 
 * <p>This class provides a common implementation for different types of Redis Streams
 * message handlers. It manages the topic and consumer group resources needed for
 * processing messages from Redis Streams.</p>
 * 
 * <p>Concrete implementations should extend this class and implement the {@link #process()}
 * method to define the specific message processing logic.</p>
 * 
 * @see RedisStreamsHandler
 * @see TopicManager
 * @see ConsumerGroupBase
 */
public abstract class AbstractRedisStreamsMessageHandler implements RedisStreamsHandler {

  /** The topic manager used to manage Redis Stream topics */
  protected final TopicManager topicManager;

  /** The consumer group used to consume messages from Redis Streams */
  protected final ConsumerGroupBase consumerGroup;

  /**
   * Constructs a new handler with a standard consumer group.
   * 
   * @param topicManager  the topic manager for managing Redis Stream topics
   * @param consumerGroup the consumer group for consuming messages with acknowledgment
   */
  public AbstractRedisStreamsMessageHandler(TopicManager topicManager, ConsumerGroup consumerGroup) {
    this.topicManager = topicManager;
    this.consumerGroup = consumerGroup;
  }

  /**
   * Constructs a new handler with a no-acknowledgment consumer group.
   * 
   * @param topicManager  the topic manager for managing Redis Stream topics
   * @param consumerGroup the consumer group for consuming messages without acknowledgment
   */
  public AbstractRedisStreamsMessageHandler(TopicManager topicManager, NoAckConsumerGroup consumerGroup) {
    this.topicManager = topicManager;
    this.consumerGroup = consumerGroup;
  }

  /**
   * Constructs a new handler with a single cluster PEL consumer group.
   * 
   * @param topicManager  the topic manager for managing Redis Stream topics
   * @param consumerGroup the single cluster PEL consumer group
   */
  public AbstractRedisStreamsMessageHandler(TopicManager topicManager, SingleClusterPelConsumerGroup consumerGroup) {
    this.topicManager = topicManager;
    this.consumerGroup = consumerGroup;
  }

}
