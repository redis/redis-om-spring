package com.redis.om.streams;

import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import redis.clients.jedis.resps.StreamEntry;

/**
 * Represents an entry in a Redis Stream topic.
 * This class encapsulates the data and metadata associated with a stream entry,
 * including the stream name, group name, entry ID, and the message content.
 */
@ToString
@EqualsAndHashCode
public class TopicEntry {
  /**
   * Creates a TopicEntry from a Redis Stream entry result.
   *
   * @param groupName The name of the consumer group
   * @param result    The entry result from Redis, containing the stream name and entry data
   * @param streamId  The identifier of the stream
   * @return A new TopicEntry instance
   */
  public static TopicEntry create(String groupName, Map.Entry<String, List<StreamEntry>> result, long streamId) {
    return new TopicEntry(result.getKey(), groupName, result.getValue().get(0), streamId);
  }

  /**
   * The name of the stream.
   */
  @Getter
  private final String streamName;

  /**
   * The name of the consumer group.
   */
  @Getter
  private final String groupName;

  /**
   * The unique identifier of this topic entry.
   */
  @Getter
  private final TopicEntryId id;

  /**
   * The message content of this topic entry, represented as key-value pairs.
   */
  @Getter
  private Map<String, String> message;

  /**
   * Constructs a TopicEntry with the specified parameters.
   *
   * @param streamName The name of the stream
   * @param groupName  The name of the consumer group
   * @param entry      The Redis StreamEntry containing the entry data
   * @param streamId   The identifier of the stream
   */
  public TopicEntry(String streamName, String groupName, StreamEntry entry, long streamId) {
    this.streamName = streamName;
    this.groupName = groupName;
    this.message = entry.getFields();
    this.id = new TopicEntryId(entry.getID(), streamId);
  }
}
