package com.redis.om.streams.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.redis.om.streams.TopicEntry;
import com.redis.om.streams.command.noack.NoAckConsumerGroup;
import com.redis.om.streams.command.serial.TopicManager;
import com.redis.om.streams.exception.TopicNotFoundException;

/**
 * Default implementation of a Redis Streams message handler that doesn't acknowledge messages.
 * 
 * <p>This handler processes messages from Redis Streams without sending acknowledgments.
 * It extends {@link AbstractRedisStreamsMessageHandler} and implements the processing
 * logic for consuming messages from a Redis Stream.</p>
 * 
 * <p>The handler is scheduled to run at fixed intervals to continuously check for
 * new messages in the stream.</p>
 * 
 * @see AbstractRedisStreamsMessageHandler
 * @see NoAckConsumerGroup
 */
public class DefaultNoAckRedisStreamsMessageHandler extends AbstractRedisStreamsMessageHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public DefaultNoAckRedisStreamsMessageHandler(TopicManager topicManager, NoAckConsumerGroup consumerGroup) {
    super(topicManager, consumerGroup);
  }

  /**
   * Processes messages from the Redis Stream.
   * 
   * <p>This method is scheduled to run at fixed intervals (2000ms) to continuously
   * check for new messages in the stream. It consumes a message from the stream
   * using the consumer group and logs information about the message.</p>
   * 
   * <p>The method returns true if a message was successfully consumed, false otherwise.</p>
   * 
   * @return true if a message was consumed, false if no message was available or an error occurred
   */
  @Override
  @Scheduled(
      fixedDelay = 2000
  )
  public boolean process() {
    try {
      TopicEntry topicEntry = consumerGroup.consume(getClass().getSimpleName());
      if (logger.isDebugEnabled()) {
        logger.debug("topicEntry: {}", topicEntry);
      }
      if (logger.isTraceEnabled() && topicEntry != null) {
        logger.trace("topicEntry.getId(): {}", topicEntry.getId());
        logger.trace("topicEntry.getGroupName(): {}", topicEntry.getGroupName());
        logger.trace("topicEntry.getStreamName(): {}", topicEntry.getStreamName());
        logger.trace("topicEntry.getMessage(): {}", topicEntry.getMessage());
      }
      return topicEntry != null;
    } catch (TopicNotFoundException e) {
      logger.error(e.getMessage());
      return false;
    }
  }
}
