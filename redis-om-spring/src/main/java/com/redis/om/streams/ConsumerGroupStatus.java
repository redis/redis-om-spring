package com.redis.om.streams;

import lombok.Value;

/**
 * Represents the status of a consumer group in Redis Streams.
 * This class provides information about the consumer group's lag, pending entries,
 * total entries in the topic, and identifiers for the topic and group.
 */
@Value
public class ConsumerGroupStatus {

  /**
   * The number of entries that the consumer group is behind the latest entry in the topic.
   */
  private final long consumerLag;

  /**
   * The number of entries that have been delivered to the consumer group but not yet acknowledged.
   */
  private final long pendingEntryCount;

  /**
   * The total number of entries in the topic.
   */
  private final long topicEntryCount;

  /**
   * The name of the topic.
   */
  private final String topicName;

  /**
   * The name of the consumer group.
   */
  private final String groupName;

  /**
   * Constructs a ConsumerGroupStatus with the specified parameters.
   *
   * @param topicName         The name of the topic
   * @param groupName         The name of the consumer group
   * @param pendingEntryCount The number of entries that have been delivered but not yet acknowledged
   * @param topicEntryCount   The total number of entries in the topic
   * @param consumerLag       The number of entries that the consumer group is behind the latest entry
   */
  public ConsumerGroupStatus(String topicName, String groupName, long pendingEntryCount, long topicEntryCount,
      long consumerLag) {
    this.topicName = topicName;
    this.groupName = groupName;
    this.pendingEntryCount = pendingEntryCount;
    this.topicEntryCount = topicEntryCount;
    this.consumerLag = consumerLag;
  }
}
