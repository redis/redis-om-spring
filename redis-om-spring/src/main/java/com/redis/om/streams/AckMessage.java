package com.redis.om.streams;

import com.redis.om.streams.utils.Util;

import lombok.Data;
import redis.clients.jedis.StreamEntryID;

/**
 * Represents an acknowledgment message for Redis Streams.
 * <p>
 * This class encapsulates the information needed to acknowledge a message in a Redis Stream,
 * including the stream name, consumer group name, and the message ID. It can be constructed
 * from different types of entries or directly with the required information.
 * </p>
 */
@Data
public class AckMessage {

  /**
   * The name of the Redis Stream.
   */
  private final String streamName;

  /**
   * The name of the consumer group.
   */
  private final String groupName;

  /**
   * The ID of the message to acknowledge.
   */
  private final TopicEntryId id;

  /**
   * Constructs an AckMessage from a TopicEntry.
   *
   * @param entry The TopicEntry containing the stream name, group name, and ID information
   */
  public AckMessage(TopicEntry entry) {
    this.streamName = entry.getStreamName();
    this.groupName = entry.getGroupName();
    this.id = entry.getId();
  }

  /**
   * Constructs an AckMessage from a PendingEntry.
   *
   * @param pendingEntry The PendingEntry containing the stream name, group name, and ID information
   */
  public AckMessage(PendingEntry pendingEntry) {
    this.streamName = pendingEntry.getStreamName();
    this.groupName = pendingEntry.getGroupName();
    this.id = new TopicEntryId(pendingEntry.getId(), pendingEntry.getStreamId());
  }

  /**
   * Constructs an AckMessage with the specified stream name, group name, and StreamEntryID.
   *
   * @param streamName The name of the Redis Stream
   * @param groupName  The name of the consumer group
   * @param id         The StreamEntryID of the message to acknowledge
   */
  public AckMessage(String streamName, String groupName, StreamEntryID id) {
    this.streamName = streamName;
    this.groupName = groupName;
    this.id = new TopicEntryId(id, Util.streamIdFromStreamName(streamName));
  }

  /**
   * Constructs an AckMessage with the specified stream name, group name, and ID as a string.
   *
   * @param streamName The name of the Redis Stream
   * @param groupName  The name of the consumer group
   * @param id         The ID of the message to acknowledge as a string
   */
  public AckMessage(String streamName, String groupName, String id) {
    this.streamName = streamName;
    this.groupName = groupName;
    this.id = new TopicEntryId(new StreamEntryID(id), Util.streamIdFromStreamName(streamName));
  }

  /**
   * Returns the string representation of the stream entry ID.
   *
   * @return The string representation of the stream entry ID
   */
  public String getStreamEntryId() {
    return id.getStreamEntryId().toString();
  }
}
