/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.KeyValue;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

public class MetricsMonitor<T extends Session> {
  private final static Logger logger = LoggerFactory.getLogger(MetricsMonitor.class);
  private final MeterRegistry meterRegistry;
  private final Optional<String> appPrefix;
  private final SessionProvider<T> sessionProvider;
  private final int numSessions;
  private final ScheduledExecutorService scheduler;
  private final Long[] largestSessionSizes;
  private final Long[] mostAccessedSessions;
  private final double[] sizeQuantiles;
  private final double[] quantiles;
  private Long numUniqueSessions = 0L;
  private long localCacheCapacity;
  private long localCacheSize;
  private long numLocalCacheEntries;
  private double averageCacheSize;

  public MetricsMonitor(MeterRegistry meterRegistry, SessionProvider<T> provider, Optional<String> appPrefix,
      int numSessions, double[] quantiles) {
    this.meterRegistry = meterRegistry;
    this.sessionProvider = provider;
    this.appPrefix = appPrefix;
    this.numSessions = numSessions;

    int period = 5;
    int initialDelay = 5;
    this.scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(this::monitorTopSessions, initialDelay, period, TimeUnit.SECONDS);
    scheduler.scheduleAtFixedRate(this::monitorSessionAccess, initialDelay, period, TimeUnit.SECONDS);
    scheduler.scheduleAtFixedRate(this::monitorSessionSizeStatistics, initialDelay, period, TimeUnit.SECONDS);
    scheduler.scheduleAtFixedRate(this::monitorUniqueSessionCount, initialDelay, period, TimeUnit.SECONDS);
    scheduler.scheduleAtFixedRate(this::monitorLocalCache, initialDelay, period, TimeUnit.SECONDS);

    this.quantiles = quantiles;
    largestSessionSizes = new Long[numSessions];
    mostAccessedSessions = new Long[numSessions];
    sizeQuantiles = new double[this.quantiles.length];
    Gauge.builder("redis.sessions.unique.sessions", () -> this.numUniqueSessions).register(this.meterRegistry);

    for (int i = 0; i < quantiles.length; i++) {
      int index = i;
      Gauge.builder("redis.session.size.quantiles", () -> sizeQuantiles[index]).tag("quantile", String.valueOf(
          quantiles[i])).register(this.meterRegistry);
    }

    for (int i = 1; i <= numSessions; i++) {
      int index = i - 1;
      largestSessionSizes[index] = 0L;
      mostAccessedSessions[index] = 0L;
      Gauge.builder("redis.session.largest", () -> largestSessionSizes[index]).tag("sessionRank", String.valueOf(i))
          .register(this.meterRegistry);

      Gauge.builder("redis.session.most.accessed", () -> mostAccessedSessions[index]).tag("sessionRank", String.valueOf(
          i)).register(this.meterRegistry);
    }

    setupLocalCacheStats();
  }

  private void setupLocalCacheStats() {
    Gauge.builder("redis.local.cache.capacity", () -> localCacheCapacity).register(this.meterRegistry);
    Gauge.builder("redis.local.cache.size", () -> localCacheSize).register(this.meterRegistry);
    Gauge.builder("redis.local.cache.num.entries", () -> numLocalCacheEntries).register(this.meterRegistry);
    Gauge.builder("redis.local.cache.average.entry.size", () -> averageCacheSize).register(this.meterRegistry);
  }

  public void monitorLocalCache() {
    LocalCacheStatistics statistics = sessionProvider.getLocalCacheStatistics();
    localCacheCapacity = statistics.getCacheCapacity();
    localCacheSize = statistics.getCacheSize();
    numLocalCacheEntries = statistics.getNumEntries();
    averageCacheSize = statistics.getAverageEntrySize();
  }

  public void monitorTopSessions() {
    try {
      Map<String, Long> topSessions = this.sessionProvider.largestSessions(this.numSessions);

      int i = 0;
      for (Map.Entry<String, Long> entry : topSessions.entrySet()) {

        this.largestSessionSizes[i] = entry.getValue();
        i++;
        if (i > this.largestSessionSizes.length) {
          break;
        }
      }
    } catch (Exception e) {
      logger.error("error checking largest sessions", e);
    }
  }

  public void monitorSessionSizeStatistics() {
    try {
      List<Double> quantileResults = this.sessionProvider.sessionSizeQuantiles(quantiles);
      for (int i = 0; i < quantileResults.size(); i++) {
        this.sizeQuantiles[i] = quantileResults.get(i);
      }

    } catch (Exception e) {
      logger.error("Encountered error while checking session size statistics", e);
    }
  }

  public void monitorUniqueSessionCount() {
    try {
      this.numUniqueSessions = sessionProvider.uniqueSessions();
    } catch (Exception e) {
      logger.error("Encountered error while checking num unique sessions", e);
    }
  }

  public void monitorSessionAccess() {
    try {
      List<KeyValue<String, Long>> mostAccessedSessions = this.sessionProvider.mostAccessedSessions();
      int i = 0;

      for (KeyValue<String, Long> session : mostAccessedSessions) {

        this.mostAccessedSessions[i] = session.getValue();
        i++;
        if (i >= this.mostAccessedSessions.length) {
          break;
        }
      }
    } catch (Exception e) {
      logger.error("error checking session access", e);
    }
  }
}
