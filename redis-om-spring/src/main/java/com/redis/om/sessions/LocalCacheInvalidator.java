/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.pubsub.RedisPubSubListener;

public class LocalCacheInvalidator<T extends Session> implements RedisPubSubListener<String, String> {
  private final static Logger logger = LoggerFactory.getLogger(LocalCacheInvalidator.class);
  private final LocalCache<T> localCache;

  public LocalCacheInvalidator(LocalCache<T> localCache) {
    this.localCache = localCache;
  }

  @Override
  public void message(String s, String s2) {
    this.localCache.removeEntry(s2, true);
  }

  @Override
  public void message(String s, String k1, String s2) {
  }

  @Override
  public void subscribed(String s, long l) {

  }

  @Override
  public void psubscribed(String s, long l) {

  }

  @Override
  public void unsubscribed(String s, long l) {

  }

  @Override
  public void punsubscribed(String s, long l) {

  }
}
