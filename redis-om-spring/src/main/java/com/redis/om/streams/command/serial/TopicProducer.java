package com.redis.om.streams.command.serial;

import java.time.Instant;
import java.util.Map;

import com.redis.om.streams.TopicEntryId;
import com.redis.om.streams.command.LuaCommandRunner;
import com.redis.om.streams.exception.InvalidMessageException;
import com.redis.om.streams.exception.ProducerTimeoutException;
import com.redis.om.streams.exception.TopicNotFoundException;
import com.redis.om.streams.utils.Util;

import lombok.Getter;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;

/**
 * A producer for Redis Streams that implements a serial topic policy.
 * 
 * This topic policy creates a logical topic consisting of one or more streams.
 * Once a maximum stream size is reached, a new stream is created.
 * For each topic, the following keys are created:
 * - Topic config key - a hash.
 * - Streams key - a list of created streams, in order.
 * - Full streams key - a set containing the names of all streams that have reached max capacity.
 * - Topic index - a sorted set. Members are stream names and scores are the last IDs written to each stream.
 * - Topic producer lock. Check this lock to determine whether producers are still writing to a given stream.
 * - Streams - the individual streams comprising this topic.
 *
 * The keys listed above are hash-keyed on topic name. Therefore, these keys will always appear together on a
 * single shard. The streams themselves are not hash-keyed and thus will be distributed across the cluster.
 */
public class TopicProducer implements com.redis.om.streams.Producer {

  /** Connection to Redis */
  private final JedisPooled connection;

  /** Runner for Lua commands */
  private final LuaCommandRunner luaCommandRunner;

  /** Flag indicating whether the producer has been initialized */
  private boolean initialized;

  /** Configuration for this topic */
  @Getter
  private SerialTopicConfig config;

  /** Name of this topic */
  @Getter
  private final String topicName;

  /** Current stream being written to */
  @Getter
  private String currentStream;

  /** ID of the current stream */
  @Getter
  private long currentStreamId;

  /**
   * Constructs a new TopicProducer for the specified topic.
   *
   * @param connection The JedisPooled connection to Redis
   * @param topicName  The name of the topic to produce to
   */
  public TopicProducer(JedisPooled connection, String topicName) {
    this.connection = connection;
    this.topicName = topicName;
    this.luaCommandRunner = new LuaCommandRunner(connection);
    this.initialized = false;
  }

  /**
   * Produces a message to the topic.
   * This method initializes the producer if necessary, publishes the message to the current stream,
   * and handles stream switching if the current stream is full.
   *
   * @param message A map containing the message fields and values
   * @return A TopicEntryId containing the ID and stream name of the produced message
   * @throws TopicNotFoundException   If the topic does not exist
   * @throws InvalidMessageException  If the message is invalid
   * @throws ProducerTimeoutException If the producer times out after multiple retries
   */
  @Override
  public TopicEntryId produce(Map<String, String> message) throws TopicNotFoundException, InvalidMessageException,
      ProducerTimeoutException {
    Instant end = Instant.now().plusSeconds(5);
    initializeStream();
    String streamToPublish = this.currentStream;
    StreamEntryID entryId = luaCommandRunner.publishSerialStreamMessage(streamToPublish, config.generateStreamTTL(),
        config.getMaxStreamLength(), config.getMinStreamTTL(), message);

    // If the entry ID is null, then we need to switch to the next stream
    // and then try to write again.
    int retries = 0;
    while (entryId == null) {
      markComplete(streamToPublish);
      setNextStream(Util.streamIdFromStreamName(streamToPublish));
      String nextStream = this.currentStream;
      streamToPublish = nextStream;
      entryId = luaCommandRunner.publishSerialStreamMessage(nextStream, config.generateStreamTTL(), config
          .getMaxStreamLength(), config.getMinStreamTTL(), message);
      retries += 1;
      if (Instant.now().isAfter(end)) {
        throw new ProducerTimeoutException(
            "Unable to produce to topic " + "within 5 seconds and " + retries + " retries. " + "To address this, increase the maximum number of" + "messages per stream and/or reduce the number of " + "parallel threads using the same producer instance.");
      }
    }

    return new TopicEntryId(entryId, streamToPublish);
  }

  /**
   * Initializes the producer by loading the topic configuration and setting the initial stream.
   * This method is synchronized to ensure thread safety during initialization.
   *
   * @throws TopicNotFoundException If the topic does not exist
   */
  private synchronized void initializeStream() throws TopicNotFoundException {
    if (!this.initialized) {
      this.config = TopicManager.loadConfig(connection, topicName);
      setNextStream();
      this.initialized = true;
    }
  }

  /**
   * Sets the next stream to use for producing messages.
   * This method is synchronized to ensure thread safety when changing streams.
   *
   * @param streamId The ID of the current stream, or -1 to get the first available stream
   */
  private synchronized void setNextStream(long streamId) {
    String response = luaCommandRunner.getNextSerialActiveActiveStream(config, streamId);
    this.currentStream = response;
    this.currentStreamId = Util.streamIdFromStreamName(response);
    Util.ensureStreamWithTTLExists(connection, currentStream, config.generateStreamTTL());
  }

  /**
   * Sets the next stream to use for producing messages, starting from the first available stream.
   * This is a convenience method that calls setNextStream(-1).
   */
  private void setNextStream() {
    setNextStream(-1);
  }

  /**
   * Marks a stream as complete (full) so that no more messages will be produced to it.
   * This method also cleans up old entries in the full streams set and stream index.
   *
   * @param streamToPublish The name of the stream to mark as complete
   */
  private void markComplete(String streamToPublish) {
    long serverTimeSeconds = Util.getServerTimeMs(connection) / 1000;
    long expiryAtSeconds = serverTimeSeconds + config.getRetentionTimeSeconds();
    connection.zadd(config.getFullStreamsKey(), expiryAtSeconds, streamToPublish);

    // Remove all old full streams entries
    String expiredTimeSeconds = String.valueOf(serverTimeSeconds - config.getRetentionTimeSeconds());
    connection.zremrangeByScore(config.getFullStreamsKey(), "-inf", expiredTimeSeconds);

    // Remove all old stream list entries
    connection.zremrangeByScore(config.getStreamIndexKey(), "-inf", expiredTimeSeconds);
  }
}
