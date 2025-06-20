package com.redis.om.streams.utils;

import static com.redis.om.streams.config.CustomConfigConstants.*;
import static java.util.Collections.singletonMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.redis.om.streams.config.ConfigManager;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XPendingParams;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.StreamPendingEntry;
import redis.clients.jedis.resps.StreamPendingSummary;

/**
 * Utility class for executing Redis Streams commands.
 * Provides methods for subscribing to streams, retrieving pending entries,
 * and building parameters for Redis Streams operations.
 */
public class StreamsCommand {

  /**
   * Factory method to create a new StreamsCommand instance.
   *
   * @param jedis the JedisPooled connection to use
   * @return a new StreamsCommand instance
   */
  public static StreamsCommand getInstance(JedisPooled jedis) {
    return new StreamsCommand(jedis);
  }

  /**
   * The JedisPooled connection used for Redis operations.
   */
  private final JedisPooled jedis;

  /**
   * Parameters for XPENDING commands.
   */
  private final XPendingParams pendingParams;

  /**
   * Parameters for XREADGROUP commands.
   */
  private final XReadGroupParams groupParams;

  /**
   * Parameters for XREAD commands.
   */
  private final XReadParams readParams;

  /**
   * Constructs a new StreamsCommand with the specified JedisPooled connection.
   * Initializes command parameters from the configuration.
   *
   * @param jedis the JedisPooled connection to use
   */
  public StreamsCommand(JedisPooled jedis) {
    Properties config = ConfigManager.INSTANCE.getStreamsConfig();

    this.jedis = jedis;
    this.pendingParams = buildXPendingParams(Integer.parseInt(String.valueOf(config.getOrDefault(
        REDIS_STREAMS_PENDING_MSG_COUNT, String.valueOf(0)))));
    this.groupParams = buildXReadGroupParams(Integer.parseInt(String.valueOf(config.getOrDefault(
        REDIS_STREAMS_CONSUMER_BLOCK_COUNT, String.valueOf(0)))), Integer.parseInt(String.valueOf(config.getOrDefault(
            REDIS_STREAMS_CONSUMER_READ_COUNT, String.valueOf(0)))), Boolean.parseBoolean(String.valueOf(config
                .getOrDefault(REDIS_STREAMS_CONSUMER_READ_ACK, Boolean.valueOf("false")))));
    this.readParams = buildXReadParams(Integer.parseInt(String.valueOf(config.getOrDefault(
        REDIS_STREAMS_CONSUMER_BLOCK_COUNT, String.valueOf(0)))), Integer.parseInt(String.valueOf(config.getOrDefault(
            REDIS_STREAMS_CONSUMER_READ_COUNT, String.valueOf(0)))));
  }

  /**
   * Subscribes to messages from the specified stream.
   * Uses the XREAD command to read entries from the stream.
   *
   * @param streamName the name of the stream to subscribe to
   * @return a list of stream entries
   */
  public List<Map.Entry<String, List<StreamEntry>>> subscribeMessage(String streamName) {
    Map<String, StreamEntryID> streamEntryIDMap = singletonMap(streamName, StreamEntryID.UNRECEIVED_ENTRY);

    return this.jedis.xread(readParams, streamEntryIDMap);
  }

  /**
   * Subscribes to messages from the specified stream as part of a consumer group.
   * Uses the XREADGROUP command to read entries from the stream.
   *
   * @param streamName    the name of the stream to subscribe to
   * @param groupName     the name of the consumer group
   * @param consumerNames the names of the consumers
   * @return a list of stream entries
   */
  public List<Map.Entry<String, List<StreamEntry>>> subscribeMessage(String streamName, String groupName,
      String... consumerNames) {
    Map<String, StreamEntryID> streamEntryIDMap = singletonMap(streamName, StreamEntryID.UNRECEIVED_ENTRY);

    return this.jedis.xreadGroup(groupName, Arrays.toString(consumerNames), groupParams, streamEntryIDMap);
  }

  /**
   * Gets a summary of pending messages for a consumer group.
   * Executes the XPENDING command: XPENDING key group
   *
   * @param streamName the name of the stream
   * @param groupName  the name of the consumer group
   * @return a summary of pending messages
   */
  public StreamPendingSummary getPendingSummary(String streamName, String groupName) {
    return this.jedis.xpending(streamName, groupName);
  }

  /**
   * Gets detailed information about pending messages for a consumer group.
   * Executes the XPENDING command: XPENDING key group [[IDLE min-idle-time] start end count [consumer]]
   *
   * @param streamName the name of the stream
   * @param groupName  the name of the consumer group
   * @return a list of pending entries
   */
  public List<StreamPendingEntry> getPendingEntry(String streamName, String groupName) {
    return this.jedis.xpending(streamName, groupName, pendingParams);
  }

  /**
   * Builds parameters for the XPENDING command.
   *
   * @param count the maximum number of entries to return
   * @return the XPendingParams object, or null if count is not positive
   */
  private XPendingParams buildXPendingParams(int count) {
    XPendingParams params = null;

    if (count > 0) {
      params = new XPendingParams().start(StreamEntryID.MINIMUM_ID).end(StreamEntryID.MAXIMUM_ID).count(count);
    }

    return params;
  }

  /**
   * Builds parameters for the XREADGROUP command.
   *
   * @param block the number of milliseconds to block waiting for new entries
   * @param count the maximum number of entries to return
   * @param ack   whether to automatically acknowledge the entries
   * @return the XReadGroupParams object
   */
  private XReadGroupParams buildXReadGroupParams(int block, int count, boolean ack) {
    XReadGroupParams params;

    if (ack) {
      params = new XReadGroupParams().block(block).count(count);
    } else {
      params = new XReadGroupParams().block(block).count(count).noAck();
    }

    return params;
  }

  /**
   * Builds parameters for the XREAD command.
   *
   * @param block the number of milliseconds to block waiting for new entries
   * @param count the maximum number of entries to return
   * @return the XReadParams object
   */
  private XReadParams buildXReadParams(int block, int count) {
    return new XReadParams().block(block).count(count);
  }
}
