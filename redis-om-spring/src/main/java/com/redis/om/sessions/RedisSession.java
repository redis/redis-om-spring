/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.om.sessions.indexing.IndexedField;
import com.redis.om.sessions.indexing.RedisIndexConfiguration;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.json.JsonPath;

public class RedisSession implements Session {
  private final static Logger logger = LoggerFactory.getLogger(RedisSession.class);
  private final Optional<Duration> maxInactiveInterval;
  private final Map<String, Object> sessionData;
  private String sessionId;
  private final Map<String, Object> updateData = new HashMap<>();
  private boolean isNew;
  private final StatefulRedisModulesConnection<String, String> connection;
  private final StatefulRedisModulesConnection<byte[], byte[]> rawConnection;
  private final Optional<String> appPrefix;
  private final RedisIndexConfiguration redisIndexConfiguration;
  private final Serializer serializer;

  RedisSession(Map<String, Object> sessionData, String sessionId, boolean isNew,
      StatefulRedisModulesConnection<String, String> connection,
      StatefulRedisModulesConnection<byte[], byte[]> rawConnection, Optional<String> appPrefix,
      RedisIndexConfiguration redisIndexConfiguration, Serializer serializer,
      RedisSessionProviderConfiguration config) {
    this.serializer = serializer;
    this.sessionData = sessionData;
    this.sessionId = sessionId;
    this.isNew = isNew;
    this.connection = connection;
    this.appPrefix = appPrefix;
    this.rawConnection = rawConnection;
    this.redisIndexConfiguration = redisIndexConfiguration;
    this.maxInactiveInterval = config.getTtl();

    if (this.isNew) {
      long currentUnixTimestamp = System.currentTimeMillis();
      this.sessionData.put(Constants.CREATED_AT_KEY, currentUnixTimestamp);
      this.sessionData.put(Constants.LAST_ACCESSED_TIME_KEY, currentUnixTimestamp);
      this.sessionData.put(Constants.LAST_MODIFIED_TIME_KEY, currentUnixTimestamp);
      this.sessionData.put(Constants.SIZE_FIELD_NAME, 0);
      maxInactiveInterval.ifPresent(d -> this.sessionData.put(Constants.MAX_INACTIVE_INTERVAL_KEY, maxInactiveInterval
          .get().get(ChronoUnit.SECONDS)));
      updateData.putAll(this.sessionData);

    } else if (this.getSize() == 0) {
      throw new IllegalArgumentException("Created a session which is not new without a defined size");
    }
  }

  public static RedisSession create(Map<String, Object> sessionData, String sessionId, boolean isNew,
      StatefulRedisModulesConnection<String, String> connection,
      StatefulRedisModulesConnection<byte[], byte[]> rawConnection, Optional<String> appPrefix,
      RedisIndexConfiguration redisIndexConfiguration, Serializer serializer,
      RedisSessionProviderConfiguration config) {
    return new RedisSession(sessionData, sessionId, isNew, connection, rawConnection, appPrefix,
        redisIndexConfiguration, serializer, config);

  }

  private SessionMetrics getSessionMetrics() {
    if (this.sessionData.containsKey(Constants.SIZE_FIELD_NAME)) {
      SessionMetrics sm = new SessionMetrics();
      sm.setSize(Long.parseLong(this.sessionData.get(Constants.SIZE_FIELD_NAME).toString()));
      return sm;
    }

    throw new IllegalStateException("Session did not contain session metrics");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId() {
    return sessionId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String changeSessionId() {
    String newSessionId = UUID.randomUUID().toString();
    return this.changeSessionId(newSessionId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String changeSessionId(String sessionId) {
    String oldSessionKey = keyName();
    String oldSessionId = this.sessionId;
    try {
      this.sessionId = sessionId;
      this.save();
    } catch (Exception e) {
      this.sessionId = oldSessionId;
      throw e;
    }

    this.connection.sync().unlink(oldSessionKey);
    return this.sessionId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> Optional<T> getAttribute(String attribute) {
    if (!sessionData.containsKey(attribute)) {
      return Optional.empty();
    }

    try {
      Object obj = this.sessionData.get(attribute);
      return Optional.of((T) obj);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getAttributeNames() {
    return Set.of(this.connection.sync().hkeys(keyName()).toArray(new String[0]));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> Long setAttribute(String attributeName, T attributeValue) {
    this.sessionData.put(attributeName, attributeValue);
    this.updateData.put(attributeName, attributeValue);
    return save();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeAttribute(String attributeName) {
    if (this.sessionData.containsKey(attributeName)) {
      this.sessionData.remove(attributeName);
      /**
       * FIXME
       * Needed to combine both Redis Sessions and Redis Cache together despite using different LettuceMod versions
       * LettuceMod 4.3.0 works well for Redis Sessions
       * LettuceMod 4.2.1 works well for Redis Cache
       * Unfortunately they differ in LettuceMod dependency, and RedisJsonCommands API.
       * The quick and dirty fix was to downgrade to version 4.2.1 (fixing Redis Cache was more complicated)
       * and implement the fieldNameToJsonPath which provides a JsonPath object.
       */
      //            this.connection.sync().jsonDel(keyName(), fieldNameToPath(attributeName));
      this.connection.sync().jsonDel(keyName(), fieldNameToJsonPath(attributeName));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Instant getCreationTime() {
    Optional<Long> creationTime = this.getAttribute(Constants.CREATED_AT_KEY);
    if (creationTime.isEmpty()) {
      throw new IllegalStateException("Creation Time not Found on Session");
    }

    return Instant.ofEpochMilli(creationTime.get());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLastAccessedTime(Instant lastAccessedTime) {
    setAttribute(Constants.LAST_ACCESSED_TIME_KEY, lastAccessedTime.toEpochMilli());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Instant getLastAccessedTime() {
    Optional<Long> lastAccessedTime = getAttribute(Constants.LAST_ACCESSED_TIME_KEY);
    if (lastAccessedTime.isEmpty()) {
      throw new IllegalStateException("Last Accessed Time not Found on Session");
    }

    return Instant.ofEpochMilli(lastAccessedTime.get());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setMaxInactiveInterval(Duration interval) {
    setAttribute(Constants.MAX_INACTIVE_INTERVAL_KEY, interval);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<Duration> getMaxInactiveInterval() {
    Optional<Long> seconds = getAttribute(Constants.MAX_INACTIVE_INTERVAL_KEY);
    if (seconds.isEmpty()) {
      return Optional.of(Duration.ofSeconds(600));
    }
    return seconds.map(Duration::ofSeconds);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isExpired() {
    Optional<Duration> maxInactiveInterval = getMaxInactiveInterval();
    return maxInactiveInterval.filter(duration -> Duration.between(getLastAccessedTime(), Instant.now()).compareTo(
        duration) > 0).isPresent();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long save() {
    long startTime = System.nanoTime();
    if (this.updateData.isEmpty() || this.updateData.size() == 1 && this.updateData.containsKey(
        Constants.LAST_ACCESSED_TIME_KEY)) {
      return this.getSize();
    }

    String[] keys = { keyName() };
    List<String> args = new ArrayList<>();
    Map<byte[], byte[]> fieldValues = new HashMap<>();
    maxInactiveInterval.ifPresent(d -> args.add(String.valueOf(d.get(ChronoUnit.SECONDS))));
    ;
    for (Map.Entry<String, Object> entry : this.updateData.entrySet()) {
      if (Constants.reservedFields.contains(entry.getKey())) {
        fieldValues.put(entry.getKey().getBytes(StandardCharsets.UTF_8), entry.getValue().toString().getBytes(
            StandardCharsets.UTF_8));
        continue;
      }
      try {
        if (this.redisIndexConfiguration.getFields().containsKey(entry.getKey())) {
          IndexedField indexedField = this.redisIndexConfiguration.getFields().get(entry.getKey());
          if (indexedField.isKnownOrDefaultClass(entry.getValue().getClass())) {
            fieldValues.put(entry.getKey().getBytes(StandardCharsets.UTF_8), entry.getValue().toString().getBytes(
                StandardCharsets.UTF_8));
          } else {
            throw new IllegalArgumentException(String.format(
                "Object provided for serialization did not match a known or default type: %s", entry.getValue()
                    .getClass().getName()));
          }
        } else {
          byte[] raw = this.serializer.Serialize(entry.getValue());
          fieldValues.put(entry.getKey().getBytes(StandardCharsets.UTF_8), raw);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    byte[][] argsArr = args.stream().map(String::getBytes).toArray(byte[][]::new);
    byte[][] keyBytesArray = Arrays.stream(keys).map(String::getBytes).toArray(byte[][]::new);

    this.rawConnection.async().hset(keys[0].getBytes(StandardCharsets.UTF_8), fieldValues);
    RedisFuture<Long> sizeFuture = this.rawConnection.async().fcall(Function.touch_key.name(), ScriptOutputType.INTEGER,
        keyBytesArray, argsArr);

    if (fieldValues.size() > 1 || !fieldValues.containsKey(Constants.LAST_ACCESSED_TIME_KEY)) {
      this.connection.async().publish(String.format(Constants.INVALIDATION_CHANNEL_FORMAT, this.sessionId),
          this.sessionId);
    }

    this.connection.flushCommands();
    this.rawConnection.flushCommands();
    this.updateData.clear();
    try {
      Long size = sizeFuture.get();
      return size;
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getSize() {
    return this.getSessionMetrics().getSize();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getLastModifiedTime() {
    return (Long) this.sessionData.get(Constants.LAST_MODIFIED_TIME_KEY);
  }

  private static String fieldNameToPath(String fieldName) {
    return String.format("$.%s", fieldName);
  }

  private static JsonPath fieldNameToJsonPath(String fieldName) {
    return new JsonPath(fieldNameToPath(fieldName));
  }

  private String keyName() {
    return buildKeyName(this.appPrefix, this.sessionId);
  }

  public static String buildKeyName(Optional<String> appPrefix, String sessionId) {
    if (appPrefix.isPresent()) {
      return String.format("%s:session:%s", appPrefix.get(), sessionId);
    }

    return String.format("session:%s", sessionId);
  }
}
