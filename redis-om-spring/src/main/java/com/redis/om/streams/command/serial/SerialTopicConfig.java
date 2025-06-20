package com.redis.om.streams.command.serial;

import java.time.Duration;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import redis.clients.jedis.StreamEntryID;

/**
 * Configuration for a serial topic in Redis Streams.
 * A serial topic is a logical topic that consists of one or more Redis Streams.
 * This class defines the properties and behavior of the topic, including retention time,
 * stream length limits, and key naming conventions.
 */
@ToString
@EqualsAndHashCode
public class SerialTopicConfig {

  /**
   * Creates a SerialTopicConfig instance from a map of configuration values.
   *
   * @param storedConfig A map containing the configuration values
   * @return A new SerialTopicConfig instance
   */
  public static SerialTopicConfig fromMap(Map<String, String> storedConfig) {
    return new SerialTopicConfig(storedConfig.get("topicName"), Long.valueOf(storedConfig.get("retentionTimeSeconds")),
        Long.valueOf(storedConfig.get("maxStreamLength")), Long.valueOf(storedConfig.get("streamCycleSeconds")),
        TTLFuzzMode.valueOf(storedConfig.get("ttlFuzzMode")));
  }

  /** Key for the index of all topics */
  public static final String TOPIC_INDEX_KEY = "__rsj:index:topics__";

  /** Key for the index of all consumer groups */
  public static final String CONSUMER_GROUP_INDEX_KEY = "__rsj:index:groups__";

  /** Special group name used during creation of new groups */
  public static final String BLANK_GROUP_FOR_CREATE = "__rsj:blank:group:c61a6c36-8850-4f16-adb9-ba3ee6d7c859__";

  /** Minimum stream entry ID */
  public static final StreamEntryID MIN_STREAM_ENTRY_ID = new StreamEntryID(0);

  /**
   * Enumeration of TTL fuzz modes.
   * RANDOM: Adds a random amount of time to the retention time
   * NONE: Uses the exact retention time
   */
  public enum TTLFuzzMode {
    /** Adds a random amount of time to the retention time */
    RANDOM,
    /** Uses the exact retention time */
    NONE
  }

  /** Field name for topic name in hash */
  public static final String TOPIC_FIELD_NAME = "topicName";

  /** Field name for group name in hash */
  public static final String GROUP_FIELD_NAME = "groupName";

  /** Default maximum number of entries in a stream before creating a new one */
  private static final int DEFAULT_MAX_STREAM_LENGTH = 250000;

  /** Default retention time for streams (8 days) */
  private static final long DEFAULT_RETENTION_TIME_SECONDS = Duration.ofDays(8).toSeconds();

  /** Default minimum time between stream cycles (1 day) */
  private static final long DEFAULT_MIN_STREAM_CYCLE_SECONDS = Duration.ofDays(1).toSeconds();

  /** Maximum value for stream index */
  public static final double MAX_STREAM_INDEX_VALUE = 9999999999999.00;

  /** The TTL fuzz mode for this topic */
  @Getter
  private final TTLFuzzMode ttlFuzzMode;

  /** Maximum number of entries in a stream before creating a new one */
  @Getter
  private final long maxStreamLength;

  /** Time in seconds between stream cycles */
  @Getter
  private final long streamCycleSeconds;

  /** Minimum time to live for streams in seconds */
  @Getter
  private final long minStreamTTL;

  /** Retention time for streams in seconds */
  @Getter
  private final long retentionTimeSeconds;

  /** Name of this topic */
  @Getter
  private final String topicName;

  /** Random number generator for TTL fuzzing */
  private Random random = new Random();

  // TODO: Validate topic config and raise exception if invalid
  /**
   * Constructs a new SerialTopicConfig with the specified topic name and default settings.
   *
   * @param topicName The name of the topic
   */
  public SerialTopicConfig(String topicName) {
    this(topicName, DEFAULT_RETENTION_TIME_SECONDS);
  }

  /**
   * Constructs a new SerialTopicConfig with the specified topic name and retention time.
   *
   * @param topicName            The name of the topic
   * @param retentionTimeSeconds The retention time in seconds
   */
  public SerialTopicConfig(String topicName, long retentionTimeSeconds) {
    this.topicName = topicName;
    this.retentionTimeSeconds = retentionTimeSeconds;
    this.maxStreamLength = DEFAULT_MAX_STREAM_LENGTH;
    this.streamCycleSeconds = DEFAULT_MIN_STREAM_CYCLE_SECONDS;
    this.minStreamTTL = retentionTimeSeconds - streamCycleSeconds;
    this.ttlFuzzMode = TTLFuzzMode.RANDOM;
  }

  /**
   * Constructs a new SerialTopicConfig with fully customized settings.
   *
   * @param topicName            The name of the topic
   * @param retentionTimeSeconds The retention time in seconds
   * @param maxStreamLength      The maximum number of entries in a stream before creating a new one
   * @param streamCycleSeconds   The time in seconds between stream cycles
   * @param fuzzMode             The TTL fuzz mode
   */
  public SerialTopicConfig(String topicName, long retentionTimeSeconds, long maxStreamLength, long streamCycleSeconds,
      TTLFuzzMode fuzzMode) {
    this.topicName = topicName;
    this.retentionTimeSeconds = retentionTimeSeconds;
    this.maxStreamLength = maxStreamLength;
    this.streamCycleSeconds = streamCycleSeconds;
    this.minStreamTTL = retentionTimeSeconds - streamCycleSeconds;
    this.ttlFuzzMode = fuzzMode;
  }

  /**
   * Generates a TTL (Time To Live) value for a stream based on the configured TTL fuzz mode.
   * If the fuzz mode is RANDOM, adds a random amount of time to the retention time.
   * If the fuzz mode is NONE, returns the exact retention time.
   *
   * @return The TTL value in seconds
   */
  public long generateStreamTTL() {
    if (ttlFuzzMode.equals(TTLFuzzMode.RANDOM)) {
      return retentionTimeSeconds + 10 + random.nextInt(60);
    } else {
      return retentionTimeSeconds;
    }
  }

  /**
   * Converts this configuration object to a map of string key-value pairs.
   * This is useful for storing the configuration in Redis.
   *
   * @return A map representation of this configuration
   */
  public Map<String, String> asMap() {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> map = objectMapper.convertValue(this, new TypeReference<Map<String, String>>() {
    });
    return map;
  }

  // TODO: Compute these on construction. No need to build new strings every time
  // these values are needed.

  /**
   * Gets the Redis key for storing this topic's configuration.
   *
   * @return The topic configuration key
   */
  public String getTopicConfigKey() {
    return getTopicConfigKeyPrefix() + "{" + topicName + "}";
  }

  /**
   * Gets the prefix for topic configuration keys.
   *
   * @return The topic configuration key prefix
   */
  public String getTopicConfigKeyPrefix() {
    return "__rsj:topic:config:";
  }

  /**
   * Gets the prefix for consumer group keys.
   *
   * @return The consumer group key prefix
   */
  public String getConsumerGroupKeyPrefix() {
    return "__rsj:group:";
  }

  /**
   * Gets the Redis key for storing a consumer group's configuration.
   *
   * @param groupName The name of the consumer group
   * @return The consumer group configuration key
   */
  public String getConsumerGroupConfigKey(String groupName) {
    return getConsumerGroupKeyPrefix() + groupName + ":config:{" + topicName + "}";
  }

  /**
   * Gets the Redis key for the set that contains all streams marked as full.
   * Full streams will not receive additional messages from a producer.
   *
   * @return The full streams key
   */
  public String getFullStreamsKey() {
    return "__rsj:topic:full_streams:{" + topicName + "}";
  }

  /**
   * Gets the name of the initial stream for this topic.
   *
   * @return The initial stream name
   */
  public String getInitialStreamName() {
    return getCurrentStreamBaseName() + getFirstStreamId();
  }

  /**
   * Gets the Redis key for the index of streams in this topic.
   *
   * @return The stream index key
   */
  public String getStreamIndexKey() {
    return "__rsj:topic:index:{" + topicName + "}";
  }

  /**
   * Gets the base name for streams in this topic.
   *
   * @return The stream base name
   */
  public String getCurrentStreamBaseName() {
    return "__rsj:topic:stream:" + topicName + ":";
  }

  /**
   * Gets the Redis key for the Pending Entries List (PEL) for a consumer group.
   *
   * @param streamName The name of the stream
   * @param groupName  The name of the consumer group
   * @return The PEL key
   */
  public String getPELKey(String streamName, String groupName) {
    return "__PEL_{" + streamName + "}_" + groupName;
  }

  /**
   * Gets the Redis key for storing the last delivered ID for a consumer group.
   *
   * @param streamName The name of the stream
   * @param groupName  The name of the consumer group
   * @return The last delivered ID key
   */
  public String getLastDeliveredIdKey(String streamName, String groupName) {
    return "__last_dlvr_id_{" + streamName + "}_" + groupName;
  }

  /**
   * Gets the Redis key for storing the number of entries read by a consumer group.
   *
   * @param streamName The name of the stream
   * @param groupName  The name of the consumer group
   * @return The entries read key
   */
  public String getEntriesReadKey(String streamName, String groupName) {
    return "__entries_read_{" + streamName + "}_" + groupName;
  }

  /**
   * Gets the field name for the latest stream ID in the topic configuration.
   *
   * @return The topic stream ID field name
   */
  public String getTopicStreamIdField() {
    return "latestStreamId";
  }

  /**
   * Gets the field name for the current stream in the consumer group configuration.
   *
   * @return The group current stream field name
   */
  public String getGroupCurrentStreamField() {
    return "currentStream";
  }

  /**
   * Gets the ID for the first stream in a topic.
   *
   * @return The first stream ID
   */
  public String getFirstStreamId() {
    return "0";
  }
}
