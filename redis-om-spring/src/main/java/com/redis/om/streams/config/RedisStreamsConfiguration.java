package com.redis.om.streams.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.redis.om.streams.Producer;
import com.redis.om.streams.command.noack.NoAckConsumerGroup;
import com.redis.om.streams.command.serial.ConsumerGroup;
import com.redis.om.streams.command.serial.SerialTopicConfig;
import com.redis.om.streams.command.serial.TopicManager;
import com.redis.om.streams.command.serial.TopicProducer;
import com.redis.om.streams.exception.InvalidTopicException;
import com.redis.om.streams.handler.DefaultNoAckRedisStreamsMessageHandler;

import redis.clients.jedis.JedisPooled;

/**
 * Configuration class for Redis Streams support.
 * 
 * <p>This configuration is automatically activated when the {@code @EnableRedisStreams}
 * annotation is used. It sets up all the necessary beans for working with Redis Streams,
 * including connection pooling, topic management, consumer groups, and message handlers.</p>
 * 
 * <p>The configuration is conditional and will only be activated if the conditions
 * specified in {@link RedisStreamsCondition} are met.</p>
 * 
 * @see com.redis.om.streams.annotation.EnableRedisStreams
 * @see RedisStreamsCondition
 */
@Configuration
@EnableScheduling
@Conditional(
  RedisStreamsCondition.class
)
public class RedisStreamsConfiguration {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Value(
    "${spring.data.redis.host}"
  )
  private String host;
  @Value(
    "${spring.data.redis.port}"
  )
  private int port;

  @Bean
  public JedisPooled jedisPooled() {
    logger.info("Creating JedisPooled");
    return new JedisPooled(host, port);
  }

  @Bean
  @Qualifier(
    "topicFoo"
  )
  public SerialTopicConfig serialTopicConfig() {
    logger.info("Creating SerialTopicConfig");
    return new SerialTopicConfig("topicFoo");
  }

  @Bean
  public TopicManager topicManager(JedisPooled jedisPooled, SerialTopicConfig serialTopicConfig) {
    logger.info("Creating TopicManager");
    try {
      return TopicManager.createTopic(jedisPooled, serialTopicConfig);
    } catch (InvalidTopicException e) {
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
  }

  @Bean
  public ConsumerGroup consumerGroup(JedisPooled jedisPooled, SerialTopicConfig serialTopicConfig) {
    logger.info("Creating ConsumerGroup");
    return new ConsumerGroup(jedisPooled, serialTopicConfig.getTopicName(), "groupFoo");
  }

  @Bean
  public Producer topicProducer(JedisPooled jedisPooled, SerialTopicConfig serialTopicConfig) {
    logger.info("Creating TopicProducer");
    return new TopicProducer(jedisPooled, serialTopicConfig.getTopicName());
  }

  @Bean
  public NoAckConsumerGroup noAckConsumerGroup(JedisPooled jedisPooled, SerialTopicConfig serialTopicConfig) {
    logger.info("Creating NoAckConsumerGroup");
    return new NoAckConsumerGroup(jedisPooled, serialTopicConfig.getTopicName(), "groupFoo");
  }

  @Bean
  public DefaultNoAckRedisStreamsMessageHandler redisStreamsHandler(TopicManager topicManager,
      NoAckConsumerGroup consumerGroup) {
    logger.info("Creating DefaultNoAckRedisStreamsMessageHandler");
    return new DefaultNoAckRedisStreamsMessageHandler(topicManager, consumerGroup);
  }

}
