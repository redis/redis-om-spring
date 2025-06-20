package com.redis.om.streams.command.serial;

import java.time.Duration;
import java.util.*;

import com.redis.om.streams.ConsumerGroupStatus;
import com.redis.om.streams.PendingEntry;
import com.redis.om.streams.TopicEntryId;
import com.redis.om.streams.command.LuaCommandRunner;
import com.redis.om.streams.exception.InvalidTopicException;
import com.redis.om.streams.exception.TopicNotFoundException;
import com.redis.om.streams.exception.TopicOrGroupNotFoundException;
import com.redis.om.streams.utils.Util;

import lombok.Getter;
import redis.clients.jedis.Connection;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.ReliableTransaction;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.search.*;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.util.Pool;

/**
 * Manages Redis Stream topics and their associated consumer groups.
 * This class provides functionality for creating, loading, and managing topics,
 * as well as working with consumer groups, pending entries, and stream operations.
 * A topic in this context is a logical entity that consists of one or more Redis Streams.
 */
public class TopicManager {

  /** Separator character for tag indexes */
  protected static final char TAG_INDEX_SEPARATOR = '¦';

  /** Configuration for this topic */
  @Getter
  protected final SerialTopicConfig config;

  /** Connection to Redis */
  protected final JedisPooled connection;

  /** Runner for Lua commands */
  protected LuaCommandRunner luaCommandRunner;

  /** Minimum entry ID for streams */
  public static final StreamEntryID MIN_ENTRY_ID = new StreamEntryID(0, 0);

  /** Maximum entry ID for streams */
  public static final StreamEntryID MAX_ENTRY_ID = new StreamEntryID(9999999999999L, 9999999L);

  /** Random number generator for various operations */
  protected static Random random = new Random();

  /**
   * Returns the configuration object for an existing named topic.
   * 
   * @param connection The JedisPooled connection to Redis
   * @param topicName  The name of the topic to load
   * @return The SerialTopicConfig for the specified topic
   * @throws TopicNotFoundException If the topic does not exist
   */
  public static SerialTopicConfig loadConfig(JedisPooled connection, String topicName) throws TopicNotFoundException {
    SerialTopicConfig config = new SerialTopicConfig(topicName);
    Map<String, String> storedConfig = connection.hgetAll(config.getTopicConfigKey());
    if (storedConfig == null || storedConfig.isEmpty()) {
      throw new TopicNotFoundException(
          "Cannot find topic: " + topicName + "\n" + "" + "Did you remember to execute: TopicManager.createTopic(connection, topicConfig) or similar?");
    }
    return SerialTopicConfig.fromMap(storedConfig);
  }

  /**
   * Gets a topic manager instance from an existing named topic.
   *
   * @param connection The JedisPooled connection to Redis
   * @param topicName  The name of the topic to load
   * @return A TopicManager instance for the specified topic
   * @throws TopicNotFoundException If the topic does not exist
   */
  public static TopicManager load(JedisPooled connection, String topicName) throws TopicNotFoundException {
    return new TopicManager(connection, loadConfig(connection, topicName));
  }

  /**
   * Creates a new topic with the given name using topic defaults.
   *
   * @param connection The JedisPooled connection to Redis
   * @param topicName  The name of the topic to create
   * @return A TopicManager instance for the newly created topic
   * @throws InvalidTopicException If the topic name is invalid
   */
  public static TopicManager createTopic(JedisPooled connection, String topicName) throws InvalidTopicException {
    SerialTopicConfig config = new SerialTopicConfig(topicName);
    return createTopic(connection, config);
  }

  /**
   * Creates a new topic with the given name and retention time using topic defaults.
   *
   * @param connection           The JedisPooled connection to Redis
   * @param topicName            The name of the topic to create
   * @param retentionTimeSeconds The retention time in seconds for messages in the topic
   * @return A TopicManager instance for the newly created topic
   * @throws InvalidTopicException If the topic name is invalid
   */
  public static TopicManager createTopic(JedisPooled connection, String topicName, long retentionTimeSeconds)
      throws InvalidTopicException {
    SerialTopicConfig config = new SerialTopicConfig(topicName, retentionTimeSeconds);
    return createTopic(connection, config);
  }

  /**
   * Creates a new topic with the provided topic configuration.
   * If the topic already exists, returns a TopicManager for the existing topic.
   * TODO: Validate that topic name does not contain '¦'
   *
   * @param connection The JedisPooled connection to Redis
   * @param config     The configuration for the topic
   * @return A TopicManager instance for the newly created or existing topic
   * @throws InvalidTopicException If the topic configuration is invalid
   */
  public static TopicManager createTopic(JedisPooled connection, SerialTopicConfig config)
      throws InvalidTopicException {
    Map<String, String> localConfig = connection.hgetAll(config.getTopicConfigKey());
    if (localConfig == null || localConfig.isEmpty()) {
      ensureTopicValid(config);
      ensureIndexesExist(connection, config);
      Pool<Connection> pool = connection.getPool();
      try (Connection con = pool.getResource()) {
        ReliableTransaction transaction = new ReliableTransaction(con);
        Map<String, String> initialConfig = config.asMap();
        transaction.hset(config.getTopicConfigKey(), initialConfig);
        transaction.exec();
      }

    }
    return new TopicManager(connection, config);
  }

  /**
   * Validates that a topic configuration is valid.
   * Checks that the topic name is not null, has the correct length,
   * and contains only valid characters.
   *
   * @param config The configuration to validate
   * @throws InvalidTopicException If the topic configuration is invalid
   */
  protected static void ensureTopicValid(SerialTopicConfig config) throws InvalidTopicException {
    String topicName = config.getTopicName();
    if (topicName == null) {
      throw new InvalidTopicException("Topic name cannot be null");
    } else {
      if (!Util.nameCorrectLength(topicName)) {
        throw new InvalidTopicException("Invalid topic name length: " + topicName
            .length() + ". Topic name must be between 0 and 300 characters.");
      }

      if (!Util.nameValid(topicName)) {
        throw new InvalidTopicException(
            "Invalid topic name: '" + topicName + "'. All topic characters must match the following regular expression: [a-zA-Z0-9._-]+");
      }
    }
  }

  /**
   * Ensures that indexes exist for the topics and consumer groups.
   * Creates the necessary RediSearch indexes if they don't already exist.
   *
   * @param connection The JedisPooled connection to Redis
   * @param config     The configuration for the topic
   */
  protected static void ensureIndexesExist(JedisPooled connection, SerialTopicConfig config) {
    FTCreateParams params = new FTCreateParams();
    params.on(IndexDataType.HASH);
    params.prefix(config.getTopicConfigKeyPrefix());
    TagField topicField = new TagField(SerialTopicConfig.TOPIC_FIELD_NAME);
    //topicField.separator(TAG_INDEX_SEPARATOR);
    topicField.caseSensitive();

    try {
      connection.ftCreate(SerialTopicConfig.TOPIC_INDEX_KEY, params, List.of(topicField));
    } catch (JedisDataException e) {
      // If an exception is thrown here, it means that the index already exists.
    }

    params = new FTCreateParams();
    params.on(IndexDataType.HASH);
    params.prefix(config.getConsumerGroupKeyPrefix());
    TagField groupField = new TagField(SerialTopicConfig.GROUP_FIELD_NAME);
    //groupField.separator(TAG_INDEX_SEPARATOR);
    groupField.caseSensitive();

    try {
      connection.ftCreate(SerialTopicConfig.CONSUMER_GROUP_INDEX_KEY, params, List.of(topicField));
    } catch (JedisDataException e) {
      // If an exception is thrown here, it means that the index already exists.
    }
  }

  /**
   * Returns the names of all non-expired topics on this Redis cluster.
   * This method searches the topic index for all topics.
   *
   * @param connection The JedisPooled connection to Redis
   * @return A list of topic names
   */
  public static List<String> getTopicNames(JedisPooled connection) {
    int limit = 10000;
    List<String> names = new ArrayList<>();
    FTSearchParams params = new FTSearchParams();
    params.limit(0, limit);
    SearchResult responses = connection.ftSearch(SerialTopicConfig.TOPIC_INDEX_KEY, "*", params);
    for (Document document : responses.getDocuments()) {
      Object response = document.get(SerialTopicConfig.TOPIC_FIELD_NAME);
      if (response != null) {
        names.add(String.valueOf(response));
      }
    }

    return names;
  }

  /**
   * Returns the names of all consumer groups for the provided topic.
   * This method searches the consumer group index for groups associated with the specified topic.
   *
   * @param connection The JedisPooled connection to Redis
   * @param topicName  The name of the topic to get groups for
   * @return A list of consumer group names
   */
  public static List<String> getGroupsForTopic(JedisPooled connection, String topicName) {
    int limit = 10000;
    List<String> names = new ArrayList<>();
    FTSearchParams params = new FTSearchParams();
    params.limit(0, limit);
    String query = "@" + SerialTopicConfig.TOPIC_FIELD_NAME + ":{" + RediSearchUtil.escape(topicName) + "}";
    SearchResult responses = connection.ftSearch(SerialTopicConfig.CONSUMER_GROUP_INDEX_KEY, query, params);
    for (Document document : responses.getDocuments()) {
      Object response = document.get(SerialTopicConfig.GROUP_FIELD_NAME);
      if (response != null) {
        names.add(String.valueOf(response));
      }
    }

    return names;
  }

  /**
   * Returns the status of all consumer groups for the provided topic.
   * This method gets the status of each consumer group associated with the specified topic.
   *
   * @param connection The JedisPooled connection to Redis
   * @param topicName  The name of the topic to get group statuses for
   * @return A list of ConsumerGroupStatus objects
   */
  public static List<ConsumerGroupStatus> getGroupStatusForTopic(JedisPooled connection, String topicName) {
    List<String> groupNames = getGroupsForTopic(connection, topicName);
    List<ConsumerGroupStatus> results = new ArrayList<>();
    for (String groupName : groupNames) {
      TopicManager manager = new TopicManager(connection, new SerialTopicConfig(topicName));
      results.add(manager.getConsumerGroupStatus(groupName));
    }

    return results;
  }

  /**
   * Removes a topic, its underlying streams, and all relevant configuration keys.
   *
   * To avoid simultaneous cleanup of streams, all streams will have an expiry set
   * within the following 24 hours, and these streams will be renamed to prevent them
   * from being associated with any future topics.
   *
   * @param connection The JedisPooled connection to Redis
   * @param topicName  The name of the topic to destroy
   * @return true if the topic was successfully destroyed, false if the topic did not exist
   */
  public static boolean destroy(JedisPooled connection, String topicName) {
    SerialTopicConfig config = new SerialTopicConfig(topicName);
    Map<String, String> localConfig = connection.hgetAll(config.getTopicConfigKey());
    if (localConfig != null) {
      config = SerialTopicConfig.fromMap(localConfig);
      TopicManager manager = new TopicManager(connection, config);
      UUID deleteId = UUID.randomUUID();
      long expireTime = 90 + random.nextLong(Duration.ofMinutes(60).toSeconds());
      List<String> groupNames = TopicManager.getGroupsForTopic(connection, topicName);
      for (String streamName : manager.getStreamNames()) {
        String deletedStreamName = "{" + streamName + "}_DELETED_" + deleteId;
        connection.expire(streamName, expireTime);
        connection.rename(streamName, deletedStreamName);
        expireTime += 90;

        for (String groupName : groupNames) {
          String pelKeyName = config.getPELKey(streamName, groupName);
          String deletedPelKeyName = "__DELETED__" + deleteId + "_" + pelKeyName;
          try {
            connection.rename(pelKeyName, deletedPelKeyName);
          } catch (JedisDataException e) {
            // If there's no such key, that's entirely okay.
          }

          // The Lua scripts create a last_delivered_id key and entries_read key for each group and stream
          // Here we delete these keys.
          connection.del(config.getEntriesReadKey(streamName, groupName));
          connection.del(config.getLastDeliveredIdKey(streamName, groupName));
        }
      }

      for (String groupName : groupNames) {
        connection.del(config.getConsumerGroupConfigKey(groupName));
      }

      connection.del(config.getStreamIndexKey());
      connection.del(config.getFullStreamsKey());
      connection.del(config.getTopicConfigKey());

      return true;
    }
    return false;
  }

  /**
   * Constructs a new TopicManager with the specified connection and configuration.
   *
   * @param connection The JedisPooled connection to Redis
   * @param config     The configuration for the topic
   */
  public TopicManager(JedisPooled connection, SerialTopicConfig config) {
    this.config = config;
    this.connection = connection;
    this.luaCommandRunner = new LuaCommandRunner(connection);
  }

  /**
   * Gets the total number of messages in the streams that make up this topic.
   *
   * @return The total number of messages in the topic
   */
  public long getTopicSize() {
    long size = 0L;
    for (String streamName : getStreamNames()) {
      size += connection.xlen(streamName);
    }
    return size;
  }

  /**
   * Returns the status of the specified consumer group.
   * The status includes information about the group's lag, pending entries,
   * and the total size of the topic.
   *
   * @param groupName The name of the consumer group
   * @return A ConsumerGroupStatus object containing the group's status
   */
  public ConsumerGroupStatus getConsumerGroupStatus(String groupName) {
    long topicSize = getTopicSize();
    long lag = 0;
    try {
      lag = getConsumerGroupLag(groupName);
    } catch (TopicOrGroupNotFoundException e) {
      lag = topicSize;
    }

    ConsumerGroupStatus status = new ConsumerGroupStatus(config.getTopicName(), groupName, getPendingEntryCount(
        groupName), topicSize, lag);

    return status;
  }

  /**
   * Gets the lag for the specified consumer group.
   * The lag is the number of messages in the topic that have not yet been processed by the group.
   *
   * @param groupName The name of the consumer group
   * @return The lag for the consumer group
   * @throws TopicOrGroupNotFoundException If the consumer group does not exist
   */
  public Long getConsumerGroupLag(String groupName) throws TopicOrGroupNotFoundException {
    String currentStream = getCurrentStreamForGroup(groupName);
    if (currentStream == null) {
      throw new TopicOrGroupNotFoundException("Cannot find consumer group: " + groupName);
    } else {
      long currentStreamLag = luaCommandRunner.getConsumerGroupLag(currentStream, groupName);
      List<String> nextStreams = getNextStreams(currentStream);
      long subsequentStreamLag = 0;
      for (String stream : nextStreams) {
        subsequentStreamLag += connection.xlen(stream);
      }
      return currentStreamLag + subsequentStreamLag;
    }
  }

  /**
   * Returns the total number of pending entries for the given consumer group.
   * Pending entries are messages that have been delivered to the group but not yet acknowledged.
   *
   * @param groupName The name of the consumer group
   * @return The number of pending entries
   */
  public long getPendingEntryCount(String groupName) {
    long count = 0;
    for (String streamName : getStreamNames()) {
      String pelKey = config.getPELKey(streamName, groupName);
      count += connection.xlen(pelKey);
    }

    return count;
  }

  /**
   * Get pending entries for this consumer group. This method returns up to 1000 pending entries, even if a number
   * greater
   * than 1000 is specified in the count. This limit prevents the user from returning large buffers from Redis.
   *
   * @param groupName
   * @return
   */
  public List<PendingEntry> getPendingEntries(String groupName) {
    PendingEntryQuery query = new PendingEntryQuery();
    return getPendingEntries(groupName, query.getStartId(), query.getEndId(), query.getMinIdleTimeMilliSeconds(), query
        .getCount());
  }

  /**
   * Get pending entries for this consumer group. This method returns up to 1000 pending entries, even if a number
   * greater
   * than 1000 is specified in the count. This limit prevents the user from returning large buffers from Redis.
   *
   * All returned pending entries will have IDs start from startId, inclusive, up to endId, exclusive.
   *
   * @param groupName
   * @param query
   * @return
   */
  public List<PendingEntry> getPendingEntries(String groupName, PendingEntryQuery query) {
    return getPendingEntries(groupName, query.getStartId(), query.getEndId(), query.getMinIdleTimeMilliSeconds(), query
        .getCount());
  }

  /**
   * Get pending entries for this consumer group. This method returns up to 1000 pending entries, even if a number
   * greater
   * than 1000 is specified in the count. This limit prevents the user from returning large buffers from Redis.
   *
   * All returned pending entries will have IDs start from startId, inclusive, up to endId, exclusive.
   *
   * @param groupName
   * @param startId
   * @param endId
   * @param count
   * @return
   */
  public List<PendingEntry> getPendingEntries(String groupName, TopicEntryId startId, TopicEntryId endId, int count) {
    int minIdleTimeSeconds = 0;
    return getPendingEntries(groupName, startId, endId, minIdleTimeSeconds, count);
  }

  /**
   * Get pending entries for this consumer group. This method returns up to 1000 pending entries, even if a number
   * greater
   * than 1000 is specified in the count. This limit prevents the user from returning large buffers from Redis.
   *
   * All returned pending entries will have IDs start from startId, inclusive, up to endId, exclusive.
   *
   * @param groupName
   * @param startId
   * @param endId
   * @param minIdleTimeMilliSeconds
   * @param count
   * @return
   */
  public List<PendingEntry> getPendingEntries(String groupName, TopicEntryId startId, TopicEntryId endId,
      int minIdleTimeMilliSeconds, int count) {
    // Don't return more than 1000 messages.
    if (count > 1000) {
      count = 1000;
    } else if (count <= 0) {
      return Collections.emptyList();
    }

    if (getTopicSize() == 0) {
      return Collections.emptyList();
    }

    // Return the first stream that may contain pending entries.
    String streamName = getStreamForID(startId);

    // We need the boundary IDs as Doubles so that we can use them for comparisons later.
    Double startIdAsDouble = idAsDouble(startId.getStreamEntryId());
    Double endIdAsDouble = idAsDouble(endId.getStreamEntryId());

    // We need a flag to mark the iteration as complete.
    boolean complete = false;

    // Get the server time so that we can calculate pending entry idle time.
    long serverTimeMs = Util.getServerTimeMs(connection);

    // Now get up to _count_ entries from the first stream.
    List<PendingEntry> pendingEntries = new ArrayList<>();
    List<StreamEntry> streamEntries = luaCommandRunner.getPending(streamName, groupName, startId.getStreamEntryId()
        .toString(), endId.getStreamEntryId().toString(), count);
    for (StreamEntry entry : streamEntries) {
      // TODO: Change comparison logic here to compare long values instead of synthetic doubles
      Double entryAsDouble = idAsDouble(entry.getID());
      if (entryAsDouble > endIdAsDouble) {
        complete = true;
        break;
      }

      if (entryAsDouble <= startIdAsDouble) {
        continue;
      }

      if (entryIdleTimeMilliSeconds(entry, serverTimeMs) >= minIdleTimeMilliSeconds) {
        pendingEntries.add(streamEntryToPendingEntry(entry, serverTimeMs, streamName, groupName));
      }
    }

    // If we have enough entries, return.
    if (pendingEntries.size() >= count || complete) {
      return pendingEntries;
    }

    streamName = getNextStream(streamName, endId);
    while (streamName != null && pendingEntries.size() < count && !complete) {
      streamEntries = luaCommandRunner.getPending(streamName, groupName, startId.getStreamEntryId().toString(), endId
          .getStreamEntryId().toString(), count - pendingEntries.size());

      // Add the pending entries one at a time until we reach the endID.
      for (StreamEntry entry : streamEntries) {
        if (idAsDouble(entry.getID()) > endIdAsDouble) {
          complete = true;
          break;
        }

        // Filter by idle time if a min idle time is provided
        if (entryIdleTimeMilliSeconds(entry, serverTimeMs) >= minIdleTimeMilliSeconds) {
          pendingEntries.add(streamEntryToPendingEntry(entry, serverTimeMs, streamName, groupName));
        }

        if (pendingEntries.size() >= count) {
          break;
        }
      }
      streamName = getNextStream(streamName, endId);
    }
    return pendingEntries;
  }

  protected long entryIdleTimeMilliSeconds(StreamEntry entry, long serverTimeMs) {
    String deliveryTime = entry.getFields().get("delivery_time");
    Long idleTimeMs = serverTimeMs - Long.valueOf(deliveryTime);
    return idleTimeMs;
  }

  protected PendingEntry streamEntryToPendingEntry(StreamEntry entry, long serverTimeMs, String streamName,
      String groupName) {
    StreamEntryID id = entry.getID();
    Map<String, String> fields = entry.getFields();
    String consumer = fields.get("consumer");
    String deliveryTime = fields.get("delivery_time");
    Long idleTimeMs = serverTimeMs - Long.valueOf(deliveryTime);
    return new PendingEntry(id, config.getTopicName(), streamName, groupName, consumer, idleTimeMs, 1L);
  }

  /**
   * Return true if the provided stream is the latest stream in the topic
   * 
   * @param streamName
   * @return True or false
   */
  public boolean isLatestStream(String streamName) {
    long streamId = Util.streamIdFromStreamName(streamName);
    return (streamId == latestStreamId());
  }

  /**
   * Return the latest generated stream ID for this topic.
   *
   * @return
   */
  public long latestStreamId() {
    String latestId = connection.hget(config.getTopicConfigKey(), config.getTopicStreamIdField());
    return Long.valueOf(latestId);
  }

  /**
   * Find the stream that must contain the provided ID, if such an ID exists in the topic.
   * 
   * @param id
   * @return
   */
  public String getStreamForID(TopicEntryId id) {
    return getStreamForId(id.getStreamId());
  }

  public String getStreamForId(long streamId) {
    return config.getCurrentStreamBaseName() + streamId;
  }

  /**
   * Return the next stream for this topic created after the provided stream.
   * 
   * @param startStream
   * @return
   */
  public String getNextStream(String startStream) {
    return getNextStream(startStream, TopicEntryId.MAX_ID);
  }

  public String getNextStream(String startStream, TopicEntryId maxId) {
    String latestId = connection.hget(config.getTopicConfigKey(), config.getTopicStreamIdField());
    long startStreamId = Util.streamIdFromStreamName(startStream);
    long latestIdLong = Long.valueOf(latestId);
    if (latestIdLong > startStreamId && latestIdLong <= maxId.getStreamId()) {
      long nextStreamId = startStreamId + 1;
      return config.getCurrentStreamBaseName() + nextStreamId;
    } else {
      return null;
    }
  }

  protected Double idAsDouble(StreamEntryID maxId) {
    return Double.valueOf(maxId.toString().replace("-", "."));
  }

  /**
   * Return all streams for this topic created after the provided stream.
   *
   * @param startStream
   * @return
   *
   *         TODO: This method is far from optimized. This should be optimized in the future.
   */
  public List<String> getNextStreams(String startStream) {
    List<String> streams = connection.zrangeByScore(config.getStreamIndexKey(), "-inf", "+inf");
    List<String> results = new ArrayList<>();
    long startStreamId = Util.streamIdFromStreamName(startStream);
    for (String stream : streams) {
      long streamId = Util.streamIdFromStreamName(stream);
      if (streamId > startStreamId) {
        results.add(stream);
      }
    }

    return results;
  }

  /**
   * Return the stream currently assigned to the provided consumer group.
   * 
   * @param groupName
   * @return
   */
  public String getCurrentStreamForGroup(String groupName) {
    return connection.hget(config.getConsumerGroupConfigKey(groupName), "currentStream");
  }

  /**
   * Return a list of all streams in the topic, ordered from oldest to newest.
   * 
   * @return
   */
  public List<String> getStreamNames() {
    return connection.zrangeByScore(config.getStreamIndexKey(), "-inf", "+inf");
  }

  public List<String> getOrderedStreamNames() {
    List<String> names = connection.zrangeByScore(config.getStreamIndexKey(), "-inf", "+inf");
    Collections.sort(names, new StreamNameComparator());
    return names;
  }

  protected static class StreamNameComparator implements Comparator<String> {
    @Override
    public int compare(String a, String b) {
      long aId = Util.streamIdFromStreamName(a);
      long bId = Util.streamIdFromStreamName(b);
      if (aId < bId)
        return -1;
      if (aId > bId)
        return 1;
      return 0;
    }
  }

  /**
   * Return the set of stream marked as full. Full streams will not receive additional messages from a producer.
   * 
   * @return
   */
  public List<String> getFullStreamNames() {
    return connection.zrangeByLex(config.getFullStreamsKey(), "-", "+");
  }

  public String createConsumerGroup(String groupName) {
    String consumerGroupConfigKey = config.getConsumerGroupConfigKey(groupName);
    Map<String, String> currentConfig = connection.hgetAll(consumerGroupConfigKey);
    if (currentConfig != null && !currentConfig.isEmpty()) {
      return currentConfig.get(config.getGroupCurrentStreamField());
    } else {
      Map<String, String> consumerGroupFields = new HashMap<>();
      consumerGroupFields.put("topicName", config.getTopicName());
      consumerGroupFields.put("groupName", groupName);
      String firstStream = getFirstUnexpiredStream();
      consumerGroupFields.put(config.getGroupCurrentStreamField(), firstStream);
      connection.hset(config.getConsumerGroupConfigKey(groupName), consumerGroupFields);
      return firstStream;
    }
  }

  public String getFirstUnexpiredStream() {
    for (String streamName : getOrderedStreamNames()) {
      if (connection.exists(streamName)) {
        return streamName;
      } else {
        connection.zrem(config.getStreamIndexKey(), streamName);
        connection.zrem(config.getFullStreamsKey(), streamName);
      }
    }

    return config.getInitialStreamName();
  }
}
