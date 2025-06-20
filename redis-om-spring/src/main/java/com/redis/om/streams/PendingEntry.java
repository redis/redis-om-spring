package com.redis.om.streams;

import com.redis.om.streams.utils.Util;

import lombok.Data;
import redis.clients.jedis.StreamEntryID;

/**
 * Represents a pending entry in a Redis Stream.
 * Pending entries are messages that have been delivered to consumers but not yet acknowledged.
 * This class encapsulates the metadata associated with such entries, including their IDs,
 * the stream and consumer group they belong to, and delivery statistics.
 */
@Data
public class PendingEntry {

  /**
   * The Redis StreamEntryID of this pending entry.
   */
  private final StreamEntryID id;

  /**
   * The name of the topic this entry belongs to.
   */
  private final String topicName;

  /**
   * The name of the stream this entry belongs to.
   */
  private final String streamName;

  /**
   * The name of the consumer that this entry was delivered to.
   */
  private final String consumerName;

  /**
   * The name of the consumer group this entry belongs to.
   */
  private final String groupName;

  /**
   * The time in milliseconds that this entry has been idle (not acknowledged).
   */
  private final Long idleTimeMs;

  /**
   * The number of times this entry has been delivered.
   */
  private final Long deliveryCount;

  /**
   * The identifier of the stream this entry belongs to.
   */
  private final Long streamId;

  /**
   * The TopicEntryId representation of this pending entry's ID.
   */
  private final TopicEntryId topicEntryId;

  /**
   * Constructs a PendingEntry with the specified parameters.
   *
   * @param id            The Redis StreamEntryID of this pending entry
   * @param topicName     The name of the topic this entry belongs to
   * @param streamName    The name of the stream this entry belongs to
   * @param groupName     The name of the consumer group this entry belongs to
   * @param consumerName  The name of the consumer that this entry was delivered to
   * @param idleTimeMs    The time in milliseconds that this entry has been idle
   * @param deliveryCount The number of times this entry has been delivered
   */
  public PendingEntry(StreamEntryID id, String topicName, String streamName, String groupName, String consumerName,
      Long idleTimeMs, Long deliveryCount) {
    this.id = id;
    this.topicName = topicName;
    this.streamName = streamName;
    this.groupName = groupName;
    this.consumerName = consumerName;
    this.idleTimeMs = idleTimeMs;
    this.deliveryCount = deliveryCount;

    this.streamId = Util.streamIdFromStreamName(streamName);
    this.topicEntryId = new TopicEntryId(id, streamId);
  }

}
