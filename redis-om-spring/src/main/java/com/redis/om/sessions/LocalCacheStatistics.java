/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import lombok.Getter;

@Getter
public class LocalCacheStatistics {
  private final long cacheCapacity;
  private final long cacheSize;
  private final long numEntries;
  private final double averageEntrySize;

  public LocalCacheStatistics(long cacheCapacity, long cacheSize, long numEntries, double averageEntrySize) {
    this.cacheCapacity = cacheCapacity;
    this.cacheSize = cacheSize;
    this.numEntries = numEntries;
    this.averageEntrySize = averageEntrySize;
  }
}
