package com.redis.om.streams.command;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redis.om.streams.TopicEntry;
import com.redis.om.streams.command.serial.SerialTopicConfig;
import com.redis.om.streams.command.serial.TopicManager;
import com.redis.om.streams.exception.TopicNotFoundException;
import com.redis.om.streams.utils.Util;

import lombok.Getter;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.Tuple;

/**
 * Abstract base class for consumer groups in Redis Streams.
 * <p>
 * This class provides common functionality for different types of consumer groups,
 * such as managing stream connections, tracking consumer group state, and handling
 * message consumption. It serves as the foundation for specific consumer group
 * implementations like {@code ConsumerGroup}, {@code NoAckConsumerGroup}, and
 * {@code SingleClusterPelConsumerGroup}.
 * <p>
 * Consumer groups in Redis Streams allow multiple consumers to cooperatively
 * consume messages from the same stream, with each message being delivered to
 * only one consumer within the group.
 */
public abstract class ConsumerGroupBase {

  /** The Jedis connection used to execute Redis commands. */
  protected final JedisPooled connection;

  /** The LuaCommandRunner used to execute Lua scripts. */
  protected final LuaCommandRunner luaCommandRunner;

  /** The configuration for the topic this consumer group is consuming from. */
  @Getter
  protected SerialTopicConfig config;

  /** The name of this consumer group. */
  @Getter
  protected final String groupName;

  /** The name of the topic this consumer group is consuming from. */
  @Getter
  protected final String topicName;

  /** The current stream this consumer group is consuming from. */
  @Getter
  protected String currentStream;

  /** The ID of the current stream this consumer group is consuming from. */
  @Getter
  protected long currentStreamId;

  /** Whether this consumer group has been initialized. */
  protected boolean initialized;

  /** The time at which the configuration should be refreshed. */
  protected Instant nextConfigurationRefresh;

  /**
   * The last reported lag (number of unprocessed messages) for this consumer group.
   * Did not utilize the lombok.getter tag because I want to be explicit with the logic.
   */
  protected long lastReportedLagForTopicAndGroup = 0;

  /**
   * Consumes a message from the topic for the specified consumer.
   *
   * @param consumerName the name of the consumer
   * @return the consumed message, or null if no message is available
   * @throws TopicNotFoundException if the topic does not exist
   */
  public abstract TopicEntry consume(String consumerName) throws TopicNotFoundException;

  /**
   * Gets the next message from the topic for the specified consumer.
   *
   * @param consumerName the name of the consumer
   * @return a list of stream entries, or null if no message is available
   */
  public abstract List<Map.Entry<String, List<StreamEntry>>> getNextMessage(String consumerName);

  /**
   * Constructs a new ConsumerGroupBase with the specified connection, topic name, and group name.
   *
   * @param connection the Jedis connection to use
   * @param topicName  the name of the topic to consume from
   * @param groupName  the name of the consumer group
   */
  public ConsumerGroupBase(JedisPooled connection, String topicName, String groupName) {
    this.connection = connection;
    this.topicName = topicName;
    this.luaCommandRunner = new LuaCommandRunner(connection);
    this.groupName = groupName;
    this.initialized = false;
  }

  /**
   * Returns the last reported lag (number of unprocessed messages) for this consumer group.
   * Note that this is a cached value, derived from a prior call to
   * {@link #getEstimatedTopicEntriesReadForGroup(JedisPooled, String, String)}.
   * 
   * @return a locally cached value for the lag for this group
   */
  public long getLastReportedLagForThisInstanceTopicAndGroup() {
    return this.lastReportedLagForTopicAndGroup;
  }

  /**
   * Calculates the current lag (number of unprocessed messages) for this consumer group.
   * This method forces the calculation of the count of read objects
   * and from that, it calculates the best estimated known lag for this group.
   *
   * @return the best possible estimated known lag for this group
   * @throws TopicNotFoundException if the topic does not exist
   */
  public long getCurrentLagForThisInstanceTopicAndGroup() throws TopicNotFoundException {
    this.getEstimatedTopicEntriesReadForGroup(this.connection, this.topicName, this.groupName);
    return this.getLastReportedLagForThisInstanceTopicAndGroup();
  }

  /**
   * Calculates the estimated number of entries read from the topic by this consumer group.
   * It also calculates the best estimated known lag and sets that value locally on this class instance.
   * 
   * @param connection the Jedis connection to use
   * @param topicName  the name of the topic
   * @param groupName  the name of the consumer group
   * @return the estimated number of entries read
   * @throws TopicNotFoundException if the topic does not exist
   */
  public long getEstimatedTopicEntriesReadForGroup(JedisPooled connection, String topicName, String groupName)
      throws TopicNotFoundException {
    initialize();
    long result = 0;
    // below, we use the entries read keys:
    //__entries_read_{__rsj:topic:stream:Topic-79342252526761:0}_consumer:noack:1
    // and the __rsj:topic:index... key and its members:
    // __rsj:topic:stream:Topic-79342252526761:0
    // __rsj:topic:stream:Topic-79342252526761:1
    // to determine total read for group & topic

    String z_topic_streams_index = "__rsj:topic:index:{" + topicName + "}";
    ScanResult<Tuple> scanResult = connection.zscan(z_topic_streams_index, "0");
    List<Tuple> tl = scanResult.getResult();
    int totalNumStreamsInGroup = 0;
    String streamName = "";
    //"GET" "__entries_read_{__rsj:topic:stream:Topic-81870797064923:0}_consumer:noack:1"
    for (Tuple t : tl) {
      streamName = t.getElement();
      String readEntriesKey = "__entries_read_{" + streamName + "}_" + groupName;
      try {
        result += Long.parseLong(connection.get(readEntriesKey));
      } catch (NumberFormatException nfe) {
        //TODO: unclear if this kind of notification is necessary or desired:
        //System.out.println("Zero reads reported for group: "+groupName+" on stream"+t.getElement());
      }
      totalNumStreamsInGroup++;
    }
    refreshEstimatedLag(connection, topicName, totalNumStreamsInGroup, streamName, result);
    return result;
  }

  /**
   * Refreshes the estimated lag (number of unprocessed messages) for this consumer group.
   * This method is only useful to a caller that has intimate knowledge of the state of the streams
   * that belong to a topic - as well as the currently known count of read entries for this group.
   * Therefore, it only makes sense to allow it to be invoked from the method that
   * retrieves the currently known count of read entries for the group.
   * 
   * @param connection                the Jedis connection to use
   * @param topicNameVal              the name of the topic
   * @param totalNumStreamsInGroup    the total number of streams in the group
   * @param lastStreamInGroup         the name of the last stream in the group
   * @param estimatedTotalEntriesRead the estimated total number of entries read
   * @throws TopicNotFoundException if the topic does not exist
   */
  protected void refreshEstimatedLag(JedisPooled connection, String topicNameVal, int totalNumStreamsInGroup,
      String lastStreamInGroup, long estimatedTotalEntriesRead) throws TopicNotFoundException {
    //related data gathering for lag in case it is wanted:
    //__rsj:topic:config:{Topic-82180446799153} <--sample topic keyname construction
    //System.out.println("ot:0502:debug ConsumerGroupBase:refreshEstimatedLag -->\n\t This.topicName == "+topicName+"   passed-in topicName is: "+topicNameVal);
    String topicConfigKeyName = "__rsj:topic:config:{" + topicNameVal + "}";
    long totalEntriesForTopic = 0;
    long lastStreamLength = 0;
    try {
      totalEntriesForTopic = Long.parseLong(connection.hget(topicConfigKeyName, "maxStreamLength"));
      totalEntriesForTopic = totalEntriesForTopic * (totalNumStreamsInGroup - 1);//don't count the most recent stream
      lastStreamLength = connection.xlen(lastStreamInGroup);
    } catch (Throwable t) {
      System.out.println(
          "ot:0502 ConsumerGroupBase:refreshEstimatedLag --> maybe weird issue reading topicConfig maxStreamLength " + topicConfigKeyName);
      t.printStackTrace();
    }
    this.lastReportedLagForTopicAndGroup = (totalEntriesForTopic + lastStreamLength) - estimatedTotalEntriesRead;
  }

  /**
   * Peeks at the next messages in the topic without consuming them.
   * This method allows you to see what messages are available without removing them from the stream.
   *
   * @param count the maximum number of messages to peek at (limited to 100)
   * @return a list of topic entries, or an empty list if no messages are available
   * @throws TopicNotFoundException if the topic does not exist
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

    final String streamName = currentStreamForGroup;
    final long streamId = currentStreamIdForGroup;

    // If the first entry's ID matches the last delivered id, then remove that entry
    if (!entries.isEmpty()) {
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
   * Active-Active databases do not appear to replicate the last-delivered-id field of a consumer
   * group. For this reason, the Lua scripts that we use store the last delivered ID for each group
   * always write the last delivered ID to a stream whose length is capped at 1. This means
   * that getting the last delivered ID for a consumer group means getting the ID of single
   * message stored in this stream. If the stream is empty or does not exist, we return
   * the minimum ID (0-0).
   *
   * @param streamName
   * @param groupName
   * @return
   */
  protected StreamEntryID getLastDeliveredId(String streamName, String groupName) {
    String lastDeliveredIdStreamName = config.getLastDeliveredIdKey(streamName, groupName);
    List<StreamEntry> results = connection.xrange(lastDeliveredIdStreamName, StreamEntryID.MINIMUM_ID,
        StreamEntryID.MAXIMUM_ID, 1);
    if (results.isEmpty()) {
      return SerialTopicConfig.MIN_STREAM_ENTRY_ID;
    } else {
      StreamEntry entry = results.get(0);
      return entry.getID();
    }
  }

  /**
   * Gets the next stream for the consumer group to read from.
   *
   * @param streamToAdvance the current stream
   * @return the name of the next stream
   */
  protected String getNextStream(String streamToAdvance) {
    return luaCommandRunner.advanceConsumerGroupStream(config, groupName, streamToAdvance);
  }

  /**
   * Initializes the consumer group.
   * This method loads the topic configuration, creates the consumer group if it doesn't exist,
   * and sets the current stream. If the configuration has expired, it refreshes the configuration
   * and updates the current stream.
   *
   * @throws TopicNotFoundException if the topic does not exist
   */
  protected synchronized void initialize() throws TopicNotFoundException {
    if (!initialized) {
      this.config = TopicManager.loadConfig(connection, topicName);
      TopicManager manager = new TopicManager(connection, config);
      String stream = manager.createConsumerGroup(groupName);
      ensureConsumerGroupExists(stream);
      setCurrentStream(stream);
      this.initialized = true;
    } else if (configurationExpired()) {
      this.config = TopicManager.loadConfig(connection, topicName);
      TopicManager manager = new TopicManager(connection, config);
      String firstUnexpiredStream = manager.getFirstUnexpiredStream();

      String latestStreamForGroup = connection.hget(config.getConsumerGroupConfigKey(groupName), config
          .getGroupCurrentStreamField());
      long firstStreamId = Util.streamIdFromStreamName(firstUnexpiredStream);
      long storedStreamId = Util.streamIdFromStreamName(latestStreamForGroup);
      if (storedStreamId > firstStreamId) {
        ensureConsumerGroupExists(latestStreamForGroup);
        setCurrentStream(latestStreamForGroup);
      } else {
        ensureConsumerGroupExists(firstUnexpiredStream);
        setCurrentStream(firstUnexpiredStream);
      }
    }
  }

  /**
   * Sets the current stream for this consumer group.
   *
   * @param streamName the name of the stream to set as current
   */
  protected void setCurrentStream(String streamName) {
    this.currentStream = streamName;
    this.currentStreamId = Util.streamIdFromStreamName(currentStream);
    setNextConfigurationRefresh();
  }

  /**
   * Sets the time at which the configuration should be refreshed.
   * This is based on the TTL of the current stream or the retention time of the topic.
   */
  protected void setNextConfigurationRefresh() {
    long currentStreamTTL = connection.ttl(currentStream);
    if (currentStreamTTL > 0) {
      this.nextConfigurationRefresh = Instant.now().plusSeconds(currentStreamTTL - 1);
    } else {
      this.nextConfigurationRefresh = Instant.now().plusSeconds(config.getRetentionTimeSeconds());
    }
  }

  /**
   * Checks if the configuration has expired and needs to be refreshed.
   *
   * @return true if the configuration has expired, false otherwise
   */
  protected boolean configurationExpired() {
    return Instant.now().isAfter(nextConfigurationRefresh);
  }

  /**
   * Checks if this consumer group has been initialized.
   *
   * @return true if initialized, false otherwise
   */
  public synchronized boolean isInitialized() {
    return initialized;
  }

  /**
   * Ensures that the consumer group exists for the specified stream.
   * If the consumer group already exists, this method does nothing.
   *
   * @param stream the name of the stream
   */
  public void ensureConsumerGroupExists(String stream) {
    try {
      connection.xgroupCreate(stream, groupName, new StreamEntryID(0), true);
    } catch (JedisDataException e) {
      // TODO: Should we confirm that the text includes "BUSYGROUP"?
    }
  }
}
