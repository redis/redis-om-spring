/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

public class LocalCache<T extends Session> {
  private static final Logger logger = LoggerFactory.getLogger(LocalCache.class);
  @Getter
  private long cacheSize;
  @Getter
  private final long capacity;
  private final LocalCacheType cacheType;
  private final Map<String, CacheEntry<T>> sessions;
  private final SortedSet<CacheEntry<T>> sessionRanking;
  private final long minSessionSize;

  void removeEntry(String id, boolean unsubscribe) {
    CacheEntry<T> entry = sessions.remove(id);
    if (entry != null) {
      sessionRanking.remove(entry);
      cacheSize -= entry.getSize();
    }
  }

  boolean addEntry(T session) {
    if (this.capacity == 0) {
      return false;
    }

    if (session.getSize() < this.minSessionSize) {
      return false;
    }

    if (!this.sessions.containsKey(session.getId()) && session.getSize() > this.capacity / 10) {
      logger.warn("Session size {} exceeded 10% of local cache allocation {}, so it will not be cached locally", session
          .getSize(), capacity);
      return false;
    }

    while (this.capacity < (this.cacheSize - session.getSize())) {
      this.trimEntry();
    }

    long score = 0;
    if (this.cacheType == LocalCacheType.LRU) {
      score = System.currentTimeMillis();
    }

    CacheEntry<T> entry = new CacheEntry<>(session, score);

    if (this.sessions.containsKey(session.getId())) {
      this.removeEntry(session.getId(), false);
    }

    this.sessions.put(session.getId(), entry);
    this.sessionRanking.add(entry);
    this.cacheSize += entry.getSize();
    return true;
  }

  Optional<T> readEntry(String id) {
    CacheEntry<T> session = sessions.get(id);
    if (session == null) {
      return Optional.empty();
    }
    sessionRanking.remove(session);
    if (session.getSession().isExpired()) {
      return Optional.empty();
    }

    if (this.cacheType == LocalCacheType.LRU) {
      session.setScore(System.currentTimeMillis());
    }

    sessionRanking.add(session);

    return Optional.of(session.getSession());
  }

  public LocalCacheStatistics getStats() {
    double averageEntrySize = sessions.values().stream().mapToDouble(s -> (double) s.getSize()).average().orElse(0);
    return new LocalCacheStatistics(capacity, cacheSize, sessions.size(), averageEntrySize);
  }

  void trimEntry() {
    CacheEntry<T> entry = sessionRanking.first();
    if (entry != null) {
      this.cacheSize -= entry.getSize();
      sessions.remove(entry.getSession().getId());
    }
  }

  public LocalCache(LocalCacheType cacheType, long capacity, long minSize) {
    this.cacheType = cacheType;
    this.sessions = new HashMap<>();
    this.sessionRanking = Collections.synchronizedSortedSet(new TreeSet<>());
    this.capacity = capacity;
    this.minSessionSize = minSize;
  }
}
