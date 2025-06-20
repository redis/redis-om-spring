package com.redis.om.streams.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redis.om.streams.command.serial.SerialTopicConfig;
import com.redis.om.streams.exception.RedisStreamsException;

import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.util.Pool;

/**
 * Utility class providing helper methods for Redis Streams operations.
 * Contains methods for configuration loading, time calculations, stream management,
 * and name validation.
 */
public class Util {

  /**
   * Maximum allowed length for stream names.
   */
  private static final short MAX_NAME_LENGTH = 300;

  /**
   * Pattern for validating stream names. Valid names contain only alphanumeric characters,
   * underscores, dots, and hyphens.
   */
  static Pattern validNamePattern = Pattern.compile("[\\w._-]+");

  /**
   * Loads configuration properties from the specified file.
   *
   * @param path the path to the file containing the configuration properties; may not be null
   * @return the loaded properties; never null
   * @throws RedisStreamsException if the file cannot be read or is not a valid properties file
   */
  public static Properties loadPropertiesFile(String path) {
    Properties properties = new Properties();

    try (InputStream inputStream = Files.newInputStream(Path.of(path))) {
      properties.load(inputStream);
    } catch (IOException e) {
      throw new RedisStreamsException(path + " is not a valid properties file. " + e);
    }
    return properties;
  }

  /**
   * Gets the current server time in milliseconds.
   * This is a workaround for the absence of the time() command in JedisPooled.
   *
   * @param conn the JedisPooled connection to use
   * @return the current server time in milliseconds
   */
  public static long getServerTimeMs(JedisPooled conn) {
    Pool<Connection> pool = conn.getPool();
    try (Connection connection = pool.getResource()) {
      Jedis jedis = new Jedis(connection);
      List<String> times = jedis.time();
      if (times.size() >= 1) {
        String micros = times.get(1);
        if (micros.length() >= 3) {
          return Long.valueOf(times.get(0) + micros.substring(0, 3));
        } else {
          return Long.valueOf(times.get(0)) + 1000;
        }
      } else {
        return 0;
      }
    }
  }

  /**
   * Calculates the expiry time in seconds based on the current server time
   * plus the specified retention time.
   *
   * @param connection           the JedisPooled connection to use
   * @param retentionTimeSeconds the number of seconds to retain the data
   * @return the calculated expiry time in seconds (unix timestamp)
   */
  public static long getExpiryAtSeconds(JedisPooled connection, long retentionTimeSeconds) {
    return (getServerTimeMs(connection) / 1000) + retentionTimeSeconds;
  }

  /**
   * Extracts the stream ID from a stream name.
   * The stream name is expected to be in a format with 5 colon-separated elements,
   * with the last element being the stream ID.
   *
   * @param streamName the name of the stream
   * @return the extracted stream ID
   * @throws RedisStreamsException if the stream name format is invalid or the ID cannot be parsed
   */
  public static long streamIdFromStreamName(String streamName) {
    String[] elements = streamName.split(":");
    if (elements.length != 5) {
      throw new RedisStreamsException("Invalid stream name: " + streamName);
    }
    long result;
    try {
      result = Long.valueOf(elements[4]);
    } catch (Exception e) {
      throw new RedisStreamsException("Cannot parse stream id from stream name: " + streamName + ". Source: " + e
          .getMessage());
    }
    return result;
  }

  /**
   * Ensures that a stream with the specified name exists and has the specified TTL.
   * If the stream doesn't exist, it creates it. If it already exists, it sets the TTL.
   *
   * @param connection the JedisPooled connection to use
   * @param streamName the name of the stream to ensure exists
   * @param ttl        the time-to-live in seconds for the stream
   */
  public static void ensureStreamWithTTLExists(JedisPooled connection, String streamName, long ttl) {
    try {
      connection.xgroupCreate(streamName, SerialTopicConfig.BLANK_GROUP_FOR_CREATE, StreamEntryID.LAST_ENTRY, true);
    } catch (JedisDataException e) {
      // If a JedisDataException is thrown, then the stream already exists.
      return;
    }

    connection.expire(streamName, ttl);
  }

  /**
   * Validates that a name matches the allowed pattern for stream names.
   *
   * @param name the name to validate
   * @return true if the name is valid, false otherwise
   */
  public static boolean nameValid(String name) {
    Matcher matcher = validNamePattern.matcher(name);
    if (matcher.find()) {
      String match = matcher.group(0);
      return match.equals(name);
    }
    return false;
  }

  /**
   * Validates that a name has the correct length (greater than 0 and less than or equal to MAX_NAME_LENGTH).
   *
   * @param name the name to validate
   * @return true if the name has the correct length, false otherwise
   */
  public static boolean nameCorrectLength(String name) {
    return ((name.length() > 0) && (name.length() <= MAX_NAME_LENGTH));
  }

  /**
   * Validates a name for use in Redis Streams.
   * This method is a placeholder for future implementation.
   *
   * @param name the name to validate
   */
  public static void validateName(String name) {
    // This method is currently empty and appears to be a placeholder for future implementation
  }
}
