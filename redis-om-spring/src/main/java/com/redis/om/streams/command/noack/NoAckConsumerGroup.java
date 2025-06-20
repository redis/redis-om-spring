package com.redis.om.streams.command.noack;

import java.util.List;
import java.util.Map;

import com.redis.om.streams.TopicEntry;
import com.redis.om.streams.command.ConsumerGroupBase;
import com.redis.om.streams.exception.TopicNotFoundException;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.resps.StreamEntry;

/**
 * A consumer group implementation that consumes messages from Redis Streams without acknowledging them.
 * This is useful for scenarios where message processing is idempotent or when acknowledgment is not required.
 */
public class NoAckConsumerGroup extends ConsumerGroupBase {

  /**
   * Constructs a new NoAckConsumerGroup.
   *
   * @param connection the Jedis connection to use
   * @param topicName  the name of the topic (stream) to consume from
   * @param groupName  the name of the consumer group
   */
  public NoAckConsumerGroup(JedisPooled connection, String topicName, String groupName) {
    super(connection, topicName, groupName);
  }

  /**
   * Consumes a message from the topic without acknowledging it.
   *
   * @param consumerName the name of the consumer
   * @return a TopicEntry containing the consumed message, or null if no message is available
   * @throws TopicNotFoundException if the topic does not exist
   */
  @Override
  public TopicEntry consume(String consumerName) throws TopicNotFoundException {
    return this.consumeWithNoAck(consumerName);
  }

  /**
   * Consumes a message from the topic without acknowledging it.
   * This method initializes the consumer group if necessary and retrieves the next message.
   *
   * @param consumerName the name of the consumer
   * @return a TopicEntry containing the consumed message, or null if no message is available
   * @throws TopicNotFoundException if the topic does not exist
   */
  protected TopicEntry consumeWithNoAck(String consumerName) throws TopicNotFoundException {
    initialize();
    List<Map.Entry<String, List<StreamEntry>>> response = noAckAndGetNextMessage(consumerName);
    if (response == null) {
      return null;
    } else {
      return TopicEntry.create(groupName, response.get(0), currentStreamId);
    }
  }

  /**
   * Gets the next message from the topic without acknowledging it.
   *
   * @param consumerName the name of the consumer
   * @return a list of stream entries, or null if no message is available
   */
  @Override
  public List<Map.Entry<String, List<StreamEntry>>> getNextMessage(String consumerName) {
    return this.noAckAndGetNextMessage(consumerName);
  }

  /**
   * Gets the next message from the topic without acknowledging it.
   * This method first tries to get a message from the current stream. If no message is available,
   * it tries to get a message from the next stream in the topic.
   *
   * @param consumerName the name of the consumer
   * @return a list of stream entries, or null if no message is available in any stream of the topic
   */
  protected List<Map.Entry<String, List<StreamEntry>>> noAckAndGetNextMessage(String consumerName) {
    List<Map.Entry<String, List<StreamEntry>>> response;

    // Make a defensive copy of the current stream name so that we
    // guarantee that we're working with the same stream name throughout this method invocation.
    String streamToUse = currentStream;
    response = luaCommandRunner.noAckAndGetStreamMessage(streamToUse, groupName, consumerName);
    if (response != null) {
      return response;
    } else {
      // See if there's a next stream that we're not using.
      // If there is, then advance to the next stream and try to get a message from it.
      String nextStream = getNextStream(streamToUse);
      if (!nextStream.equals(streamToUse)) {
        ensureConsumerGroupExists(nextStream);
        setCurrentStream(nextStream);
        response = luaCommandRunner.noAckAndGetStreamMessage(nextStream, groupName, consumerName);
        return response;
      }
    }
    return null;
  }
}
