package com.redis.om.streams.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.redis.om.streams.AckMessage;
import com.redis.om.streams.TopicEntry;
import com.redis.om.streams.annotation.RedisStreamConsumer;
import com.redis.om.streams.command.noack.NoAckConsumerGroup;
import com.redis.om.streams.command.serial.ConsumerGroup;
import com.redis.om.streams.command.singleclusterpel.SingleClusterPelConsumerGroup;
import com.redis.om.streams.exception.TopicNotFoundException;

import jakarta.annotation.PostConstruct;

@Component
public abstract class RedisStreamsConsumer {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private ApplicationContext ctx;

  @PostConstruct
  private void init() {
    logger.info("{} init", getClass().getSimpleName());
  }

  protected Object getConsumerGroup() {
    RedisStreamConsumer annotation = getClass().getAnnotation(RedisStreamConsumer.class);
    String beanName;
    if (annotation.autoAck()) {
      if (annotation.cluster()) {
        beanName = annotation.groupName() + "SingleClusterPelConsumerGroup";
        return ctx.getBean(beanName, SingleClusterPelConsumerGroup.class);
      } else {
        beanName = annotation.groupName() + "ConsumerGroup";
        return ctx.getBean(beanName, ConsumerGroup.class);
      }
    } else {
      beanName = annotation.groupName() + "NoAckConsumerGroup";
      return ctx.getBean(beanName, NoAckConsumerGroup.class);
    }
  }

  protected TopicEntry consume() {
    RedisStreamConsumer annotation = getClass().getAnnotation(RedisStreamConsumer.class);
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

  protected boolean acknowledge(TopicEntry topicEntry) {
    if (topicEntry == null) {
      logger.warn("Skipping acknowledge because TopicEntry is null.");
      return false;
    }
    RedisStreamConsumer annotation = getClass().getAnnotation(RedisStreamConsumer.class);
    if (annotation.autoAck()) {
      if (annotation.cluster()) {
        SingleClusterPelConsumerGroup consumerGroup = (SingleClusterPelConsumerGroup) getConsumerGroup();
        return consumerGroup.acknowledge(new AckMessage(topicEntry));
      } else {
        ConsumerGroup consumerGroup = (ConsumerGroup) getConsumerGroup();
        return consumerGroup.acknowledge(new AckMessage(topicEntry));
      }
    } else {
      logger.warn("Ignoring acknowledge of topic {}", topicEntry);
    }
    return false;
  }

  protected String getConsumerName(String annotationConsumerName) {
    return ObjectUtils.isEmpty(annotationConsumerName) ? getClass().getSimpleName() : annotationConsumerName;
  }

}
