/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.time.Duration;
import java.util.Optional;

import com.redis.om.sessions.indexing.RedisIndexConfiguration;
import com.redis.om.sessions.serializers.JdkSerializer;

import lombok.Getter;

@Getter
public class RedisSessionProviderConfiguration {
  private final RedisIndexConfiguration indexConfiguration;
  private final long localCacheMaxSize;
  private final long minLocalRecordSize;
  private final Serializer serializer;
  private final Optional<String> appPrefix;
  private final Optional<Duration> ttl;

  private RedisSessionProviderConfiguration(RedisIndexConfiguration indexConfiguration, long localCacheMaxSize,
      long minLocalRecordSize, Serializer serializer, Optional<String> appPrefix, Optional<Duration> ttl) {
    this.indexConfiguration = indexConfiguration;
    this.localCacheMaxSize = localCacheMaxSize;
    this.minLocalRecordSize = minLocalRecordSize;
    this.serializer = serializer;
    this.appPrefix = appPrefix;
    this.ttl = ttl;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private RedisIndexConfiguration indexConfiguration = RedisIndexConfiguration.builder().build();
    private long localCacheMaxSize = 0;
    private long minLocalRecordSize = 0;
    private Serializer serializer = new JdkSerializer();
    private Optional<String> appPrefix = Optional.empty();
    private Optional<Duration> ttl = Optional.of(Duration.ofMinutes(30));

    public Builder indexConfiguration(RedisIndexConfiguration indexConfiguration) {
      this.indexConfiguration = indexConfiguration;
      return this;
    }

    public Builder localCacheMaxSize(long localCacheMaxSize) {
      this.localCacheMaxSize = localCacheMaxSize;
      return this;
    }

    public Builder minLocalRecordSize(long minLocalRecordSize) {
      this.minLocalRecordSize = minLocalRecordSize;
      return this;
    }

    public Builder serializer(Serializer serializer) {
      this.serializer = serializer;
      return this;
    }

    public Builder appPrefix(String appPrefix) {
      this.appPrefix = Optional.of(appPrefix);
      return this;
    }

    public Builder appPrefix(Optional<String> appPrefix) {
      this.appPrefix = appPrefix;
      return this;
    }

    public Builder ttl(Duration duration) {
      this.ttl = Optional.of(duration);
      return this;
    }

    public Builder ttlSeconds(long secondsToLive) {
      this.ttl = Optional.of(Duration.ofSeconds(secondsToLive));
      return this;
    }

    public Builder ttlMinutes(long minutesToLive) {
      this.ttl = Optional.of(Duration.ofMinutes(minutesToLive));
      return this;
    }

    public Builder ttlHours(long hoursToLive) {
      this.ttl = Optional.of(Duration.ofHours(hoursToLive));
      return this;
    }

    public Builder ttlDays(long days) {
      this.ttl = Optional.of(Duration.ofDays(days));
      return this;
    }

    public RedisSessionProviderConfiguration build() {
      return new RedisSessionProviderConfiguration(indexConfiguration, localCacheMaxSize, minLocalRecordSize,
          serializer, appPrefix, ttl);
    }
  }
}
