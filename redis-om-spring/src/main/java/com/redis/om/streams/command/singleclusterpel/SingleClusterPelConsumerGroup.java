package com.redis.om.streams.command.singleclusterpel;

import java.util.List;
import java.util.Map;

import com.redis.om.streams.AckMessage;
import com.redis.om.streams.TopicEntry;
import com.redis.om.streams.command.ConsumerGroupBase;
import com.redis.om.streams.exception.TopicNotFoundException;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.resps.StreamEntry;

/**
 * A specialized implementation of ConsumerGroupBase that manages consumer groups in a single cluster
 * with Pending Entry List (PEL) functionality. This class provides methods for consuming messages,
 * acknowledging messages, and managing stream operations in a single Redis cluster environment.
 */
public class SingleClusterPelConsumerGroup extends ConsumerGroupBase {
  /**
   * Constructs a new SingleClusterPelConsumerGroup with the specified connection, topic name, and group name.
   *
   * @param connection The JedisPooled connection to the Redis server
   * @param topicName  The name of the topic
   * @param groupName  The name of the consumer group
   */
  public SingleClusterPelConsumerGroup(JedisPooled connection, String topicName, String groupName) {
    super(connection, topicName, groupName);
  }

  /**
   * Consumes a message from the topic using default ACK behavior.
   * This means client code must ACK at some point to indicate complete processing.
   * Any ACK will only be for the local DB and will not be propagated to Active-Active peers.
   *
   * @param consumerName The name of the consumer
   * @return A TopicEntry containing the consumed message, or null if no message is available
   * @throws TopicNotFoundException If the specified topic does not exist
   */
  @Override
  public TopicEntry consume(String consumerName) throws TopicNotFoundException {
    return consumeSingleCluster(consumerName);
  }

  /**
   * Acknowledges a message that has been processed.
   * For this class, no Active-Active replicated PEL occurs.
   * 
   * @param ack The AckMessage containing information about the message to acknowledge
   * @return true if the message was successfully acknowledged, false otherwise
   */
  public boolean acknowledge(AckMessage ack) {
    //List<StreamEntryID> streamEntryIDS = List.of(new StreamEntryID(ack.getStreamEntryId());
    long ackValue = connection.xack(ack.getStreamName(), groupName, new StreamEntryID(ack.getStreamEntryId()));
    return ackValue == 1;
  }

  /**
   * Gets the next message for the specified consumer.
   *
   * @param consumerName The name of the consumer
   * @return A list of map entries containing stream entries, or null if no message is available
   */
  @Override
  public List<Map.Entry<String, List<StreamEntry>>> getNextMessage(String consumerName) {
    return this.singleClusterGetNextMessage(consumerName);
  }

  /**
   * Consumes a message from the topic in a single cluster environment.
   * This is a private helper method used by the public consume method.
   *
   * @param consumerName The name of the consumer
   * @return A TopicEntry containing the consumed message, or null if no message is available
   * @throws TopicNotFoundException If the specified topic does not exist
   */
  private TopicEntry consumeSingleCluster(String consumerName) throws TopicNotFoundException {
    initialize();
    List<Map.Entry<String, List<StreamEntry>>> response = singleClusterGetNextMessage(consumerName);
    if (response == null) {
      return null;
    } else {
      return TopicEntry.create(groupName, response.get(0), currentStreamId);
    }
  }

  /**
   * Gets the next message for the specified consumer from a single cluster environment.
   * This method first tries to get a message from the current stream. If no message is available,
   * it checks if there's a next stream and tries to get a message from it.
   *
   * @param consumerName The name of the consumer
   * @return A list of map entries containing stream entries, or null if no message is available
   */
  protected List<Map.Entry<String, List<StreamEntry>>> singleClusterGetNextMessage(String consumerName) {
    List<Map.Entry<String, List<StreamEntry>>> response;

    // Make a defensive copy of the current stream name so that we
    // guarantee that we're working with the same stream name throughout this method invocation.
    String streamToUse = currentStream;
    response = luaCommandRunner.singleDBPELGetStreamMessage(streamToUse, groupName, consumerName);
    if (response != null) {
      return response;
    } else {
      // See if there's a next stream that we're not using.
      // If there is, then advance to the next stream and try to get a message from it.
      String nextStream = getNextStream(streamToUse);
      if (!nextStream.equals(streamToUse)) {
        ensureConsumerGroupExists(nextStream);
        setCurrentStream(nextStream);
        response = luaCommandRunner.singleDBPELGetStreamMessage(nextStream, groupName, consumerName);
        return response;
      }
    }
    return null;
  }
}
