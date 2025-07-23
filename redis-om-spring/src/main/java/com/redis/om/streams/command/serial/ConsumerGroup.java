package com.redis.om.streams.command.serial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redis.om.streams.AckMessage;
import com.redis.om.streams.TopicEntry;
import com.redis.om.streams.command.ConsumerGroupBase;
import com.redis.om.streams.exception.TopicNotFoundException;
import com.redis.om.streams.utils.Util;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.resps.StreamEntry;

/**
 * Implementation of a Redis Stream consumer group that processes messages serially.
 * This class extends ConsumerGroupBase and provides functionality for consuming,
 * acknowledging, and peeking at messages in a Redis Stream.
 */
public class ConsumerGroup extends ConsumerGroupBase {

  /**
   * Constructs a new ConsumerGroup instance.
   *
   * @param connection The JedisPooled connection to Redis
   * @param topicName  The name of the topic to consume from
   * @param groupName  The name of the consumer group
   */
  public ConsumerGroup(JedisPooled connection, String topicName, String groupName) {
    super(connection, topicName, groupName);
  }

  /**
   * Consumes a message from the Redis Stream as the specified consumer.
   * This method initializes the consumer group if necessary, retrieves the next message,
   * and converts it to a TopicEntry.
   *
   * @param consumerName The name of the consumer within the group
   * @return A TopicEntry containing the consumed message, or null if no message is available
   * @throws TopicNotFoundException If the topic does not exist
   */
  @Override
  public TopicEntry consume(String consumerName) throws TopicNotFoundException {
    initialize();
    List<Map.Entry<String, List<StreamEntry>>> response = getNextMessage(consumerName);
    if (response == null) {
      return null;
    } else {
      return TopicEntry.create(groupName, response.get(0), currentStreamId);
    }
  }

  /**
   * Acknowledges a message that has been processed by the consumer group.
   * This removes the message from the pending entries list (PEL).
   *
   * @param ack The AckMessage containing information about the message to acknowledge
   * @return true if the message was successfully acknowledged, false otherwise
   */
  public boolean acknowledge(AckMessage ack) {
    return luaCommandRunner.ackMessage(ack.getStreamName(), ack.getGroupName(), ack.getStreamEntryId());
  }

  /**
   * Peeks at messages in the stream without consuming them.
   * This method allows viewing a specified number of messages that would be delivered
   * to this consumer group, starting from the last delivered ID.
   *
   * @param count The maximum number of messages to peek (limited to 100)
   * @return A list of TopicEntry objects representing the messages
   * @throws TopicNotFoundException If the topic does not exist
   */
  public List<TopicEntry> peek(int count) throws TopicNotFoundException {
    initialize();

    if (count <= 0 || !isInitialized()) {
      return Collections.emptyList();
    }

    // TODO: Address this limitation. We don't want to hard code this.
    if (count > 100) {
      count = 100;
    }

    SerialTopicConfig config;
    try {
      config = TopicManager.loadConfig(connection, topicName);
    } catch (TopicNotFoundException e) {
      return Collections.emptyList();
    }
    TopicManager manager = new TopicManager(connection, config);

    if (manager.getTopicSize() == 0) {
      return Collections.emptyList();
    }

    String currentStreamForGroup = manager.getCurrentStreamForGroup(groupName);
    long currentStreamIdForGroup = Util.streamIdFromStreamName(currentStreamForGroup);
    long latestStreamId = manager.latestStreamId();
    StreamEntryID lastDeliveredId = getLastDeliveredId(currentStreamForGroup, groupName);

    // TODO: Get the lag for the group as an optimization. If lag is >= count,
    // TODO: then we might not need to look at any subsequent streams

    List<TopicEntry> resultStreamEntries = new ArrayList<>();

    // Because the lastDeliveredId will be included in the result set, we need to
    // add one to the number of responses to return. This works around the
    // fact that there's no way to add the exclusive operator "(" to the lastDeliveredId
    // in Jedis.
    List<StreamEntry> entries = connection.xrange(currentStreamForGroup, lastDeliveredId, StreamEntryID.MAXIMUM_ID,
        count + 1);

    final String streamName = new String(currentStreamForGroup);
    final long streamId = currentStreamIdForGroup;

    // If the first entry's ID matches the last delivered id, then remove that entry
    if (entries.size() >= 1) {
      if (entries.get(0).getID().equals(lastDeliveredId)) {
        entries.remove(0);
      }
    }

    resultStreamEntries.addAll(entries.stream().map(e -> new TopicEntry(streamName, groupName, e, streamId)).toList());

    while (resultStreamEntries.size() < count && (currentStreamIdForGroup < latestStreamId)) {
      currentStreamIdForGroup += 1;
      currentStreamForGroup = manager.getStreamForId(currentStreamIdForGroup);
      final long sid = currentStreamIdForGroup;
      final String sname = currentStreamForGroup;
      int entriesNeeded = count - resultStreamEntries.size();
      entries = connection.xrange(currentStreamForGroup, StreamEntryID.MINIMUM_ID, StreamEntryID.MAXIMUM_ID,
          entriesNeeded);
      resultStreamEntries.addAll(entries.stream().map(e -> new TopicEntry(sname, groupName, e, sid)).toList());
    }

    return resultStreamEntries;
  }

  /**
   * Gets the next message from the stream for the specified consumer.
   * If no message is available in the current stream, it attempts to get a message
   * from the next stream in the topic.
   *
   * @param consumerName The name of the consumer within the group
   * @return A list containing the next message, or null if no message is available
   */
  @Override
  public List<Map.Entry<String, List<StreamEntry>>> getNextMessage(String consumerName) {
    List<Map.Entry<String, List<StreamEntry>>> response;

    // Make a defensive copy of the current stream name so that we
    // guarantee that we're working with the same stream name throughout this method invocation.
    String streamToUse = new String(currentStream);
    try {
      response = luaCommandRunner.getStreamMessage(streamToUse, groupName, consumerName);
    } catch (JedisDataException e) {
      if (e.getMessage().contains("NOGROUP")) {
        response = null;
      } else {
        throw e;
      }
    }

    if (response != null) {
      return response;
    } else {
      // See if there's a next stream that we're not using.
      // If there is, then advance to the next stream and try to get a message from it.
      String nextStream = getNextStream(streamToUse);
      if (!nextStream.equals(streamToUse)) {
        ensureConsumerGroupExists(nextStream);
        setCurrentStream(nextStream);
        response = luaCommandRunner.getStreamMessage(nextStream, groupName, consumerName);
        return response;
      }
    }
    return null;
  }

  /**
   * Sets the current stream for this consumer group.
   * Updates the current stream name and ID, and schedules the next configuration refresh.
   *
   * @param streamName The name of the stream to set as current
   */
  public void setCurrentStream(String streamName) {
    this.currentStream = streamName;
    this.currentStreamId = Util.streamIdFromStreamName(currentStream);
    setNextConfigurationRefresh();
  }
}
