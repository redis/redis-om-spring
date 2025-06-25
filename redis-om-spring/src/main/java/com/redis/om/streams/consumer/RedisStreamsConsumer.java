package com.redis.om.streams.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ObjectUtils;

import com.redis.om.streams.AckMessage;
import com.redis.om.streams.TopicEntry;
import com.redis.om.streams.annotation.RedisStreamConsumer;
import com.redis.om.streams.command.noack.NoAckConsumerGroup;
import com.redis.om.streams.command.serial.ConsumerGroup;
import com.redis.om.streams.command.singleclusterpel.SingleClusterPelConsumerGroup;
import com.redis.om.streams.exception.TopicNotFoundException;

import jakarta.annotation.PostConstruct;

/**
 * Abstract base class for Redis Streams consumers.
 * <p>
 * This class provides the core functionality for consuming messages from Redis Streams
 * and acknowledging them. It works in conjunction with the {@link RedisStreamConsumer}
 * annotation to configure how messages are consumed.
 * <p>
 * Concrete implementations should extend this class and implement the necessary
 * business logic for processing messages.
 */
public abstract class RedisStreamsConsumer implements ApplicationContextAware {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private ApplicationContext applicationContext;

  /**
   * Sets the Spring application context.
   * <p>
   * This method is called by the Spring container to inject the application context.
   * It's used internally to access beans and other Spring resources.
   *
   * @param applicationContext the Spring application context
   * @throws BeansException if an error occurs during bean instantiation
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  /**
   * Initializes the consumer after construction.
   * <p>
   * This method is called automatically after the bean is constructed and
   * dependencies are injected.
   */
  @PostConstruct
  private void init() {
    logger.info("{} init", getClass().getSimpleName());
  }

  /**
   * Gets the appropriate consumer group implementation based on the annotation configuration.
   * <p>
   * This method determines which type of consumer group to use based on the
   * autoAck and cluster settings in the {@link RedisStreamConsumer} annotation.
   *
   * @return the consumer group implementation
   */
  private Object getConsumerGroup() {
    RedisStreamConsumer annotation = getClass().getAnnotation(RedisStreamConsumer.class);
    assert annotation != null;
    String beanName;
    if (annotation.autoAck()) {
      if (annotation.cluster()) {
        beanName = annotation.groupName() + "SingleClusterPelConsumerGroup";
        return applicationContext.getBean(beanName, SingleClusterPelConsumerGroup.class);
      } else {
        beanName = annotation.groupName() + "ConsumerGroup";
        return applicationContext.getBean(beanName, ConsumerGroup.class);
      }
    } else {
      beanName = annotation.groupName() + "NoAckConsumerGroup";
      return applicationContext.getBean(beanName, NoAckConsumerGroup.class);
    }
  }

  /**
   * Consumes a message from the Redis Stream.
   * <p>
   * This method reads a message from the Redis Stream based on the configuration
   * specified in the {@link RedisStreamConsumer} annotation. It handles different
   * consumer group implementations based on whether auto-acknowledgment is enabled
   * and whether Redis is deployed as a cluster.
   *
   * @return the consumed message as a {@link TopicEntry}, or null if no message could be consumed
   */
  protected TopicEntry consume() {
    RedisStreamConsumer annotation = getClass().getAnnotation(RedisStreamConsumer.class);
    assert annotation != null;
    if (annotation.autoAck()) {
      if (annotation.cluster()) {
        try {
          SingleClusterPelConsumerGroup consumerGroup = (SingleClusterPelConsumerGroup) getConsumerGroup();
          return consumerGroup.consume(getConsumerName(annotation.consumerName()));
        } catch (TopicNotFoundException e) {
          logger.error(e.getMessage(), e);
        }
      } else {
        ConsumerGroup consumerGroup = (ConsumerGroup) getConsumerGroup();
        try {
          return consumerGroup.consume(getConsumerName(annotation.consumerName()));
        } catch (TopicNotFoundException e) {
          logger.error(e.getMessage(), e);
        }
      }
    } else {
      NoAckConsumerGroup consumerGroup = (NoAckConsumerGroup) getConsumerGroup();
      try {
        return consumerGroup.consume(getConsumerName(annotation.consumerName()));
      } catch (TopicNotFoundException e) {
        logger.error(e.getMessage(), e);
      }
    }
    return null;
  }

  /**
   * Acknowledges a message that has been processed.
   * <p>
   * This method acknowledges that a message has been successfully processed,
   * which removes it from the pending entries list (PEL) in Redis Streams.
   * The behavior depends on the configuration in the {@link RedisStreamConsumer} annotation.
   * If auto-acknowledgment is disabled, this method will log a debug message and return false.
   *
   * @param topicEntry the message to acknowledge
   * @return true if the message was successfully acknowledged, false otherwise
   */
  protected boolean acknowledge(TopicEntry topicEntry) {
    if (topicEntry == null) {
      logger.debug("Skipping acknowledge because TopicEntry is null.");
      return false;
    }
    RedisStreamConsumer annotation = getClass().getAnnotation(RedisStreamConsumer.class);
    assert annotation != null;
    if (annotation.autoAck()) {
      if (annotation.cluster()) {
        SingleClusterPelConsumerGroup consumerGroup = (SingleClusterPelConsumerGroup) getConsumerGroup();
        return consumerGroup.acknowledge(new AckMessage(topicEntry));
      } else {
        ConsumerGroup consumerGroup = (ConsumerGroup) getConsumerGroup();
        return consumerGroup.acknowledge(new AckMessage(topicEntry));
      }
    } else {
      logger.debug("Ignoring acknowledge of topic {}", topicEntry);
    }
    return false;
  }

  /**
   * Gets the consumer name to use for Redis Streams operations.
   * <p>
   * If a consumer name is specified in the annotation, that name is used.
   * Otherwise, the simple name of the implementing class is used as the consumer name.
   *
   * @param annotationConsumerName the consumer name from the annotation
   * @return the consumer name to use
   */
  private String getConsumerName(String annotationConsumerName) {
    return ObjectUtils.isEmpty(annotationConsumerName) ? getClass().getSimpleName() : annotationConsumerName;
  }

}
