package com.redis.om.spring.indexing;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service for managing ephemeral (temporary) indexes with TTL support.
 * Ephemeral indexes are automatically deleted after a specified time period.
 */
@Component
public class EphemeralIndexService implements DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(EphemeralIndexService.class);

  private final RediSearchIndexer indexer;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
  private final ConcurrentHashMap<String, EphemeralIndexInfo> ephemeralIndexes = new ConcurrentHashMap<>();

  /**
   * Constructs a new EphemeralIndexService.
   *
   * @param indexer the RediSearchIndexer to use for index operations
   */
  @Autowired
  public EphemeralIndexService(RediSearchIndexer indexer) {
    this.indexer = indexer;
  }

  /**
   * Creates an ephemeral index that will be automatically deleted after the specified TTL.
   *
   * @param entityClass the entity class for the index
   * @param indexName   the name of the index to create
   * @param ttl         the time-to-live for the index
   * @return true if the index was created successfully, false otherwise
   */
  public boolean createEphemeralIndex(Class<?> entityClass, String indexName, Duration ttl) {
    logger.info(String.format("Creating ephemeral index %s for %s with TTL %s", indexName, entityClass.getName(), ttl));

    try {
      // Create the index
      String keyPrefix = entityClass.getSimpleName().toLowerCase() + ":ephemeral:";
      indexer.createIndexFor(entityClass, indexName, keyPrefix);

      // Schedule deletion
      ScheduledFuture<?> deletionFuture = scheduler.schedule(() -> {
        deleteEphemeralIndex(entityClass, indexName);
      }, ttl.toMillis(), TimeUnit.MILLISECONDS);

      // Track the ephemeral index
      EphemeralIndexInfo info = new EphemeralIndexInfo(entityClass, indexName, Instant.now(), ttl, deletionFuture);
      ephemeralIndexes.put(indexName, info);

      logger.info(String.format("Successfully created ephemeral index %s", indexName));
      return true;
    } catch (Exception e) {
      logger.error(String.format("Failed to create ephemeral index %s: %s", indexName, e.getMessage()));
      return false;
    }
  }

  /**
   * Extends the TTL of an existing ephemeral index.
   *
   * @param indexName the name of the index to extend
   * @param newTtl    the new time-to-live duration
   * @return true if the TTL was extended successfully, false otherwise
   */
  public boolean extendTTL(String indexName, Duration newTtl) {
    EphemeralIndexInfo info = ephemeralIndexes.get(indexName);
    if (info == null) {
      logger.warn(String.format("Cannot extend TTL for non-ephemeral index %s", indexName));
      return false;
    }

    try {
      // Cancel the existing deletion task
      if (info.deletionFuture != null && !info.deletionFuture.isDone()) {
        info.deletionFuture.cancel(false);
      }

      // Schedule new deletion
      ScheduledFuture<?> newDeletionFuture = scheduler.schedule(() -> {
        deleteEphemeralIndex(info.entityClass, indexName);
      }, newTtl.toMillis(), TimeUnit.MILLISECONDS);

      // Update the tracking info
      info.ttl = newTtl;
      info.deletionFuture = newDeletionFuture;
      info.extendedAt = Instant.now();

      logger.info(String.format("Extended TTL for ephemeral index %s to %s", indexName, newTtl));
      return true;
    } catch (Exception e) {
      logger.error(String.format("Failed to extend TTL for index %s: %s", indexName, e.getMessage()));
      return false;
    }
  }

  /**
   * Checks if an index is tracked as ephemeral.
   *
   * @param indexName the name of the index to check
   * @return true if the index is ephemeral, false otherwise
   */
  public boolean isEphemeralIndex(String indexName) {
    return ephemeralIndexes.containsKey(indexName);
  }

  /**
   * Deletes an ephemeral index immediately.
   *
   * @param entityClass the entity class of the index
   * @param indexName   the name of the index to delete
   */
  private void deleteEphemeralIndex(Class<?> entityClass, String indexName) {
    try {
      logger.info(String.format("Deleting ephemeral index %s", indexName));

      // Drop the index
      indexer.dropIndexFor(entityClass, indexName);

      // Remove from tracking
      EphemeralIndexInfo removed = ephemeralIndexes.remove(indexName);
      if (removed != null && removed.deletionFuture != null && !removed.deletionFuture.isDone()) {
        removed.deletionFuture.cancel(false);
      }

      logger.info(String.format("Successfully deleted ephemeral index %s", indexName));
    } catch (Exception e) {
      logger.error(String.format("Failed to delete ephemeral index %s: %s", indexName, e.getMessage()));
    }
  }

  /**
   * Cleans up all ephemeral indexes and shuts down the scheduler.
   */
  @Override
  public void destroy() {
    logger.info("Shutting down EphemeralIndexService");

    // Cancel all scheduled deletions
    ephemeralIndexes.values().forEach(info -> {
      if (info.deletionFuture != null && !info.deletionFuture.isDone()) {
        info.deletionFuture.cancel(false);
      }
    });

    // Clear tracking
    ephemeralIndexes.clear();

    // Shutdown scheduler
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Internal class to track ephemeral index information.
   */
  private static class EphemeralIndexInfo {
    final Class<?> entityClass;
    final String indexName;
    final Instant createdAt;
    Duration ttl;
    ScheduledFuture<?> deletionFuture;
    Instant extendedAt;

    EphemeralIndexInfo(Class<?> entityClass, String indexName, Instant createdAt, Duration ttl,
        ScheduledFuture<?> deletionFuture) {
      this.entityClass = entityClass;
      this.indexName = indexName;
      this.createdAt = createdAt;
      this.ttl = ttl;
      this.deletionFuture = deletionFuture;
    }
  }
}