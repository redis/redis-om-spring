package com.redis.om.streams.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.redis.om.streams.TopicEntry;
import com.redis.om.streams.annotation.RedisStreamConsumer;

import jakarta.annotation.PostConstruct;

@RedisStreamConsumer(
    topicName = "topicFoo", groupName = "groupFoo", autoAck = false
)
public class NoAckFooRedisStreamsConsumer extends RedisStreamsConsumer {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @PostConstruct
  private void init() {
    logger.info("{} init", getClass().getSimpleName());
  }

  @Scheduled(
      fixedDelayString = "${redis.streams.fixed-delay:1000}"
  )
  public boolean process() {
    TopicEntry topicEntry = consume();
    if (topicEntry != null) {
      logger.info("{} processing topic: {}", getClass().getSimpleName(), topicEntry);
    }
    return true;
  }
}
