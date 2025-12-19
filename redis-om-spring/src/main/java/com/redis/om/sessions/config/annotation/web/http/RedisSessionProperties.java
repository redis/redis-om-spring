/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.config.annotation.web.http;

import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
  RedisSessionProperties.CONFIG_PREFIX
)
public class RedisSessionProperties {
  public static final String CONFIG_PREFIX = "redis";
  private String host = "localhost";
  private Optional<String> prefix = Optional.empty();
  private int port = 6379;
  private double[] sessionSizeQuantiles = new double[] { .5, .75, .9, .99, 1 };
  private Cache cache;

  public double[] getSessionSizeQuantiles() {
    return sessionSizeQuantiles;
  }

  public void setSessionSizeQuantiles(double[] sessionSizeQuantiles) {
    this.sessionSizeQuantiles = sessionSizeQuantiles;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Optional<String> getPrefix() {
    return prefix;
  }

  public void setPrefix(Optional<String> prefix) {
    this.prefix = prefix;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public Cache getCache() {
    return cache;
  }

  public void setCache(Cache cache) {
    this.cache = cache;
  }

  public static class Cache {
    private long cap;
    private int min;

    public long getCap() {
      return cap;
    }

    public void setCap(long cap) {
      this.cap = cap;
    }

    public int getMin() {
      return min;
    }

    public void setMin(int min) {
      this.min = min;
    }
  }

}
