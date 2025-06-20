package com.redis.om.streams.handler;

import com.redis.om.streams.AckMessage;
import com.redis.om.streams.TopicEntry;
import com.redis.om.streams.command.serial.ConsumerGroup;
import com.redis.om.streams.command.serial.TopicManager;
import com.redis.om.streams.exception.TopicNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Default implementation of a Redis Streams message handler with acknowledgment support.
 * 
 * <p>This handler consumes messages from Redis Streams using a consumer group that
 * requires explicit message acknowledgment. It processes messages on a fixed schedule
 * and logs information about the consumed messages.</p>
 * 
 * <p>The handler uses the consumer group's acknowledge mechanism to confirm successful
 * message processing, ensuring that messages are not lost in case of failures.</p>
 * 
 * @see AbstractRedisStreamsMessageHandler
 * @see ConsumerGroup
 * @see TopicManager
 */
public class DefaultRedisStreamsMessageHandler extends AbstractRedisStreamsMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Constructs a new DefaultRedisStreamsMessageHandler.
     * 
     * @param topicManager the topic manager for managing Redis Stream topics
     * @param consumerGroup the consumer group for consuming messages with acknowledgment
     */
    public DefaultRedisStreamsMessageHandler(TopicManager topicManager, ConsumerGroup consumerGroup) {
        super(topicManager, consumerGroup);
    }

    /**
     * Processes messages from a Redis Stream with acknowledgment.
     * 
     * <p>This method is scheduled to run at fixed intervals (every 2000ms). It consumes
     * a message from the Redis Stream using the consumer group, logs information about
     * the message at different log levels, and acknowledges the message to confirm
     * successful processing.</p>
     * 
     * <p>If the topic is not found, a RuntimeException is thrown.</p>
     * 
     * @return true if a message was successfully processed and acknowledged, false otherwise
     * @throws RuntimeException if the topic is not found
     */
    @Override
    @Scheduled(fixedDelay = 2000)
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
            return topicEntry != null && ((ConsumerGroup) consumerGroup).acknowledge(new AckMessage(topicEntry));
        } catch (TopicNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
