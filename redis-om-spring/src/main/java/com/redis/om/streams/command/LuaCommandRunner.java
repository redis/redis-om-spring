package com.redis.om.streams.command;

import static redis.clients.jedis.Protocol.Command.EVALSHA;

import java.nio.charset.StandardCharsets;
import java.util.*;

import com.redis.om.streams.command.serial.SerialTopicConfig;
import com.redis.om.streams.exception.InvalidMessageException;
import com.redis.om.streams.exception.TopicOrGroupNotFoundException;
import com.redis.om.streams.utils.Util;

import redis.clients.jedis.*;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.util.SafeEncoder;

/**
 * A utility class for executing Lua scripts on Redis.
 * <p>
 * This class loads and executes Lua scripts defined in the {@link LuaCommand} enum.
 * It provides methods for various Redis Streams operations such as publishing messages,
 * reading messages, acknowledging messages, and managing consumer groups.
 * <p>
 * The class uses the Jedis client to interact with Redis and execute the Lua scripts.
 */
public class LuaCommandRunner {

  /** Maximum number of key-value pairs allowed in a published message. */
  private static final int MAX_PUBLISH_MESSAGE_KEYS = 2000;

  /**
   * Factory method to create a new LuaCommandRunner instance.
   *
   * @param jedis the JedisPooled connection to use
   * @return a new LuaCommandRunner instance
   */
  public static LuaCommandRunner getInstance(JedisPooled jedis) {
    return new LuaCommandRunner(jedis);
  }

  /** The Jedis connection used to execute Redis commands. */
  private final JedisPooled connection;

  /** Map of LuaCommand to SHA1 hash of the loaded script. */
  private final Map<LuaCommand, String> commandMap;

  /**
   * Constructs a new LuaCommandRunner with the specified Jedis connection.
   * Loads all Lua scripts defined in the LuaCommand enum.
   *
   * @param connection the JedisPooled connection to use
   */
  public LuaCommandRunner(JedisPooled connection) {
    this.connection = connection;
    this.commandMap = loadScripts(this.connection);
  }

  /**
   * Advances a consumer group to the next stream if necessary.
   *
   * @param config        the topic configuration
   * @param groupName     the name of the consumer group
   * @param currentStream the stream to advance from. If this is not the same as the current stream, stream will not
   *                      be advanced.
   * @return the name of the next stream that the consumer group should use. The Lua function will
   *         advance the stream only if the current stream is no longer in use and a new stream exists.
   */
  public String advanceConsumerGroupStream(SerialTopicConfig config, String groupName, String currentStream) {
    String sha1 = this.commandMap.get(LuaCommand.SERIAL_ADVANCE_CONSUMER_STREAM);
    if (currentStream == null) {
      currentStream = "";
    }
    long currentStreamId = Util.streamIdFromStreamName(currentStream);
    Object response = executeLuaCommand(sha1, List.of(config.getTopicConfigKey(), config.getConsumerGroupConfigKey(
        groupName), config.getStreamIndexKey(), config.getFullStreamsKey()),

        List.of(groupName, currentStream, String.valueOf(currentStreamId), String.valueOf(config
            .getRetentionTimeSeconds()), config.getGroupCurrentStreamField(), config.getTopicStreamIdField(), config
                .getCurrentStreamBaseName()));
    return response.toString();
  }

  /**
   * Gets the next stream in an active-active configuration.
   *
   * @param config          the topic configuration
   * @param currentStreamId the ID of the current stream
   * @return the name of the next stream
   */
  public String getNextSerialActiveActiveStream(SerialTopicConfig config, long currentStreamId) {
    String sha1 = this.commandMap.get(LuaCommand.NEXT_SERIAL_ACTIVE_ACTIVE_STREAM);
    Object response = executeLuaCommand(sha1, List.of(config.getTopicConfigKey(), config.getStreamIndexKey(), config
        .getFullStreamsKey()), List.of(config.getTopicStreamIdField(), config.getCurrentStreamBaseName(), String
            .valueOf(config.getRetentionTimeSeconds()), String.valueOf(currentStreamId), String.valueOf(Util
                .getExpiryAtSeconds(connection, config.getRetentionTimeSeconds()))));
    return response.toString();
  }

  /**
   * Gets the next stream in an active-active configuration, starting from the beginning.
   *
   * @param config the topic configuration
   * @return the name of the next stream
   */
  public String getNextSerialActiveActiveStream(SerialTopicConfig config) {
    return getNextSerialActiveActiveStream(config, -1);
  }

  /**
   * Publishes a message to a serial stream.
   *
   * @param streamName           the name of the stream to publish to
   * @param retentionTimeSeconds the number of seconds to retain the message
   * @param maxStreamLength      the maximum number of entries in the stream
   * @param streamSwitchTTL      the time-to-live for stream switching
   * @param message              the message to publish as a map of key-value pairs
   * @return the ID of the published message
   * @throws InvalidMessageException if the message is null, empty, or contains null keys or values,
   *                                 or if the message contains more than MAX_PUBLISH_MESSAGE_KEYS key-value pairs
   */
  public StreamEntryID publishSerialStreamMessage(String streamName, long retentionTimeSeconds, long maxStreamLength,
      long streamSwitchTTL, Map<String, String> message) throws InvalidMessageException {
    List<String> items = new ArrayList<>();
    items.add(streamName);
    items.add(String.valueOf(retentionTimeSeconds));
    items.add(String.valueOf(maxStreamLength));
    items.add(String.valueOf(streamSwitchTTL));

    if (message == null) {
      throw new InvalidMessageException("Message may not be null.");
    }

    if (message.isEmpty()) {
      throw new InvalidMessageException("Message may not be empty.");
    }

    Set<String> keys = message.keySet();

    if (keys.size() > MAX_PUBLISH_MESSAGE_KEYS) {
      throw new InvalidMessageException("Message contains " + keys
          .size() + " keys. May not exceed " + MAX_PUBLISH_MESSAGE_KEYS);
    }

    for (String key : keys) {
      if (key == null) {
        throw new InvalidMessageException("Message may not contain any null keys.");
      }
      items.add(key);
      String value = message.get(key);
      if (value == null) {
        throw new InvalidMessageException("Message may not contain any null values.");
      }
      items.add(message.get(key));
    }

    return connection.executeCommand(readStreamEntry(this.commandMap.get(LuaCommand.SERIAL_PUBLISH_MESSAGE), 1, items
        .toArray(String[]::new)));
  }

  /**
   * Gets a message from a stream for a consumer in a consumer group.
   *
   * @param streamName   the name of the stream to read from
   * @param groupName    the name of the consumer group
   * @param consumerName the name of the consumer
   * @return a list of stream entries, or null if no messages are available
   */
  public List<Map.Entry<String, List<StreamEntry>>> getStreamMessage(String streamName, String groupName,
      String consumerName) {
    return connection.executeCommand(readStreamMessageObject(this.commandMap.get(LuaCommand.READGROUP), 1, streamName,
        groupName, consumerName));
  }

  /**
   * Gets pending entries (messages that were delivered but not yet acknowledged) for a consumer group.
   * Uses default range and count parameters.
   *
   * @param streamName the name of the stream
   * @param groupName  the name of the consumer group
   * @return a list of pending stream entries
   */
  public List<StreamEntry> getPending(String streamName, String groupName) {
    return connection.executeCommand(readListObject(this.commandMap.get(LuaCommand.PENDING), 1, streamName, groupName,
        "-", "+", "1000"));
  }

  /**
   * Gets pending entries (messages that were delivered but not yet acknowledged) for a consumer group
   * within the specified ID range and count.
   *
   * @param streamName the name of the stream
   * @param groupName  the name of the consumer group
   * @param startId    the minimum ID to include in the results
   * @param endId      the maximum ID to include in the results
   * @param count      the maximum number of entries to return
   * @return a list of pending stream entries
   */
  public List<StreamEntry> getPending(String streamName, String groupName, String startId, String endId, int count) {
    try {
      return connection.executeCommand(readListObject(this.commandMap.get(LuaCommand.PENDING), 1, streamName, groupName,
          startId, endId, String.valueOf(count)));
    } catch (JedisDataException e) {
      if (e.getMessage().contains("NOGROUP")) {
        return Collections.emptyList();
      } else {
        throw e;
      }
    }
  }

  /**
   * Gets the lag (number of unprocessed messages) for a consumer group.
   *
   * @param streamName the name of the stream
   * @param groupName  the name of the consumer group
   * @return the number of unprocessed messages, or null if the information is not available
   * @throws TopicOrGroupNotFoundException if the topic or group does not exist
   */
  public Long getConsumerGroupLag(String streamName, String groupName) throws TopicOrGroupNotFoundException {
    String sha1 = this.commandMap.get(LuaCommand.LAG);
    try {
      Object response = executeLuaCommand(sha1, List.of(streamName), List.of(groupName));
      if (response != null) {
        return Long.valueOf(response.toString());
      }
    } catch (JedisDataException e) {
      throw new TopicOrGroupNotFoundException("Cannot find topic or group: " + e.getMessage());
    }
    return null;
  }

  /**
   * Acknowledges that a message has been processed by a consumer group.
   *
   * @param streamName the name of the stream
   * @param groupName  the name of the consumer group
   * @param messageID  the ID of the message to acknowledge
   * @return true if the message was acknowledged, false otherwise
   */
  public boolean ackMessage(String streamName, String groupName, String messageID) {
    String sha1 = this.commandMap.get(LuaCommand.ACK);
    Object response = executeLuaCommand(sha1, List.of(streamName), List.of(groupName, messageID));
    return response.toString().equals("1");
  }

  /**
   * Sets the last delivered ID for a consumer group.
   *
   * @param streamName the name of the stream
   * @param groupName  the name of the consumer group
   * @param messageID  the ID to set as the last delivered ID
   * @return the result of the operation
   */
  public String setID(String streamName, String groupName, String messageID) {
    String sha1 = this.commandMap.get(LuaCommand.GROUP_SET_ID);
    Object response = executeLuaCommand(sha1, List.of(streamName), List.of(groupName, messageID));
    return response.toString();
  }

  /**
   * Executes a Lua command on Redis.
   *
   * @param commandSha1 the SHA1 hash of the Lua script to execute
   * @param keys        the Redis keys to pass to the script
   * @param args        the arguments to pass to the script
   * @return the result of the script execution
   */
  private Object executeLuaCommand(String commandSha1, List<String> keys, List<String> args) {
    return connection.evalsha(commandSha1, keys, args);
  }

  /**
   * Gets a message from a stream for a consumer in a consumer group without requiring acknowledgment.
   * The message will be delivered to the consumer but will not be added to the pending entries list.
   *
   * @param streamName   the name of the stream to read from
   * @param groupName    the name of the consumer group
   * @param consumerName the name of the consumer
   * @return a list of stream entries, or null if no messages are available
   */
  public List<Map.Entry<String, List<StreamEntry>>> noAckAndGetStreamMessage(String streamName, String groupName,
      String consumerName) {
    return connection.executeCommand(readStreamMessageObject(this.commandMap.get(LuaCommand.NOACK_READGROUP), 1,
        streamName, groupName, consumerName));
  }

  /**
   * Gets a message from a stream for a consumer in a consumer group with single cluster pending entry list.
   * This is used in a single database cluster configuration where the pending entries list is maintained
   * only on the local database and not replicated to other databases in the cluster.
   *
   * @param streamName   the name of the stream to read from
   * @param groupName    the name of the consumer group
   * @param consumerName the name of the consumer
   * @return a list of stream entries, or null if no messages are available
   */
  public List<Map.Entry<String, List<StreamEntry>>> singleDBPELGetStreamMessage(String streamName, String groupName,
      String consumerName) {
    return connection.executeCommand(readStreamMessageObject(this.commandMap.get(LuaCommand.SINGLE_DB_PEL_READGROUP), 1,
        streamName, groupName, consumerName));
  }

  /*
  private void scriptChecksum() {
      MessageDigest digest;
      try {
          digest = MessageDigest.getInstance("SHA-256");
  
      } catch (NoSuchAlgorithmException e) {
          throw new RuntimeException(e);
      }
      Arrays.stream(LuaCommand.values()).sorted().forEach(command -> {
              digest.update(command.getScript().getBytes(StandardCharsets.UTF_8));
      });
      digest.toString();
  
      Arrays.stream(LuaCommand.values()).forEach(command -> {
          command.getScript()
          if (command.script == null || command.script.isEmpty()) {
              throw new RuntimeException("Missing script content for script: " + command);
          }
          String sha1 = jedis.scriptLoad(command.script);
      });
  }
  */

  /**
   * Loads all Lua scripts defined in the LuaCommand enum into Redis.
   *
   * @param connection the JedisPooled connection to use
   * @return a map of LuaCommand to SHA1 hash of the loaded script
   * @throws RuntimeException if a script is null or empty
   */
  private Map<LuaCommand, String> loadScripts(JedisPooled connection) {
    Map<LuaCommand, String> sha1Map = new HashMap<>();
    Arrays.stream(LuaCommand.values()).forEach(command -> {
      if (command.script == null || command.script.isEmpty()) {
        throw new RuntimeException("Missing script content for script: " + command);
      }
      String sha1 = connection.scriptLoad(command.script);
      sha1Map.put(command, new String(SafeEncoder.encode(sha1), StandardCharsets.UTF_8));
    });
    return sha1Map;
  }

  /**
   * Creates command arguments for Jedis internal functionality.
   * Necessary for the Jedis internal functionality provided below.
   * 
   * @param command the protocol command to create arguments for
   * @return the command arguments
   */
  protected CommandArguments commandArguments(ProtocolCommand command) {
    return new CommandArguments(command);
  }

  /**
   * Creates a command object to read a stream-like response from a Lua script.
   * Method using Jedis internals to read a stream-like response from a Lua script.
   *
   * @param sha1     the SHA1 hash of the Lua script to execute
   * @param keyCount the number of keys to pass to the script
   * @param params   the parameters to pass to the script
   * @return a command object to read a stream-like response
   */
  private CommandObject<List<Map.Entry<String, List<StreamEntry>>>> readStreamMessageObject(String sha1, int keyCount,
      String... params) {
    return new CommandObject<>(commandArguments(EVALSHA).add(sha1).add(keyCount).addObjects((Object[]) params),
        BuilderFactory.STREAM_READ_RESPONSE);
  }

  /**
   * Creates a command object to read a list of stream entries from a Lua script.
   * Method using Jedis internals to read a stream entry from a Lua script.
   *
   * @param sha1     the SHA1 hash of the Lua script to execute
   * @param keyCount the number of keys to pass to the script
   * @param params   the parameters to pass to the script
   * @return a command object to read a list of stream entries
   */
  private CommandObject<List<StreamEntry>> readListObject(String sha1, int keyCount, String... params) {
    return new CommandObject<>(commandArguments(EVALSHA).add(sha1).add(keyCount).addObjects((Object[]) params),
        BuilderFactory.STREAM_ENTRY_LIST);
  }

  /**
   * Creates a command object to read a stream entry ID from a Lua script.
   *
   * @param sha1     the SHA1 hash of the Lua script to execute
   * @param keyCount the number of keys to pass to the script
   * @param params   the parameters to pass to the script
   * @return a command object to read a stream entry ID
   */
  private CommandObject<StreamEntryID> readStreamEntry(String sha1, int keyCount, String... params) {
    return new CommandObject<>(commandArguments(EVALSHA).add(sha1).add(keyCount).addObjects((Object[]) params),
        BuilderFactory.STREAM_ENTRY_ID);
  }

  /**
   * Creates a command object to read a list of strings from a Lua script.
   *
   * @param sha1     the SHA1 hash of the Lua script to execute
   * @param keyCount the number of keys to pass to the script
   * @param params   the parameters to pass to the script
   * @return a command object to read a list of strings
   */
  private CommandObject<List<String>> readMapEntry(String sha1, int keyCount, String... params) {
    return new CommandObject<>(commandArguments(EVALSHA).add(sha1).add(keyCount).addObjects((Object[]) params),
        BuilderFactory.STRING_LIST);
  }
}
