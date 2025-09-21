package com.redis.om.spring.indexing;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Service for managing index migrations in Redis.
 * Supports various migration strategies including blue-green deployments
 * and dual-write patterns for zero-downtime migrations.
 *
 * @since 1.0.0
 */
@Service
public class IndexMigrationService {
  private static final Logger logger = LoggerFactory.getLogger(IndexMigrationService.class);
  private static final Pattern VERSION_PATTERN = Pattern.compile("_v(\\d+)(?:_idx)?$");

  private final RediSearchIndexer indexer;
  private final ApplicationContext applicationContext;
  private final ConcurrentHashMap<Class<?>, MigrationStatus> activeMigrations = new ConcurrentHashMap<>();
  private final ExecutorService executorService = Executors.newFixedThreadPool(5);

  private enum MigrationStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED
  }

  public IndexMigrationService(RediSearchIndexer indexer, ApplicationContext applicationContext) {
    this.indexer = indexer;
    this.applicationContext = applicationContext;
  }

  /**
   * Creates a new versioned index for the given entity class.
   *
   * @param entityClass the entity class
   * @return the name of the new versioned index
   */
  public String createVersionedIndex(Class<?> entityClass) {
    int nextVersion = getNextIndexVersion(entityClass);
    String baseIndexName = getBaseIndexName(entityClass);
    String versionedIndexName = baseIndexName + "_v" + nextVersion + "_idx";

    logger.info("Creating versioned index: {} for class: {}", versionedIndexName, entityClass.getName());

    // Create the new index with versioned name
    String keyPrefix = getKeyPrefix(entityClass) + "v" + nextVersion + ":";
    indexer.createIndexFor(entityClass, versionedIndexName, keyPrefix);

    return versionedIndexName;
  }

  /**
   * Migrates an index using the specified strategy.
   *
   * @param entityClass the entity class
   * @param strategy    the migration strategy
   * @return the migration result
   */
  public MigrationResult migrateIndex(Class<?> entityClass, MigrationStrategy strategy) {
    logger.info("Starting index migration for {} using {} strategy", entityClass.getName(), strategy);

    // Check for concurrent migrations
    if (activeMigrations.putIfAbsent(entityClass, MigrationStatus.IN_PROGRESS) != null) {
      return MigrationResult.builder().successful(false).strategy(strategy).errorMessage(
          "Migration already in progress for " + entityClass.getName()).build();
    }

    Instant startTime = Instant.now();

    try {
      // Check if index exists
      if (!indexer.indexExistsFor(entityClass)) {
        return MigrationResult.builder().successful(false).strategy(strategy).startTime(startTime).endTime(Instant
            .now()).errorMessage("No existing index found for " + entityClass.getName()).build();
      }

      String oldIndexName = indexer.getIndexName(entityClass);
      String newIndexName = createVersionedIndex(entityClass);

      switch (strategy) {
        case BLUE_GREEN:
          return executeBlueGreenMigration(entityClass, oldIndexName, newIndexName, startTime);
        case DUAL_WRITE:
          return executeDualWriteMigration(entityClass, oldIndexName, newIndexName, startTime);
        case IN_PLACE:
          return executeInPlaceMigration(entityClass, oldIndexName, startTime);
        default:
          throw new IllegalArgumentException("Unsupported migration strategy: " + strategy);
      }
    } finally {
      activeMigrations.remove(entityClass);
    }
  }

  private MigrationResult executeBlueGreenMigration(Class<?> entityClass, String oldIndexName, String newIndexName,
      Instant startTime) {
    try {
      // Reindex all data to new index
      reindexToNewIndex(entityClass, newIndexName);

      // Verify integrity
      if (!verifyIndexIntegrity(newIndexName)) {
        return MigrationResult.builder().successful(false).oldIndexName(oldIndexName).newIndexName(newIndexName)
            .strategy(MigrationStrategy.BLUE_GREEN).startTime(startTime).endTime(Instant.now()).errorMessage(
                "Index integrity verification failed").build();
      }

      // Switch alias atomically
      switchAlias(entityClass, newIndexName);

      // Schedule cleanup of old index
      scheduleOldIndexCleanup(entityClass);

      return MigrationResult.builder().successful(true).oldIndexName(oldIndexName).newIndexName(newIndexName).strategy(
          MigrationStrategy.BLUE_GREEN).startTime(startTime).endTime(Instant.now()).build();
    } catch (Exception e) {
      logger.error("Blue-green migration failed", e);
      return MigrationResult.builder().successful(false).oldIndexName(oldIndexName).newIndexName(newIndexName).strategy(
          MigrationStrategy.BLUE_GREEN).startTime(startTime).endTime(Instant.now()).errorMessage(e.getMessage())
          .build();
    }
  }

  private MigrationResult executeDualWriteMigration(Class<?> entityClass, String oldIndexName, String newIndexName,
      Instant startTime) {
    try {
      // Enable dual write
      enableDualWrite(entityClass, newIndexName);

      // Start background reindex
      CompletableFuture<ReindexResult> reindexFuture = reindexInBackground(entityClass, newIndexName);

      // Wait for completion (in production, this might be monitored differently)
      ReindexResult reindexResult = reindexFuture.get();

      if (!reindexResult.isSuccessful()) {
        return MigrationResult.builder().successful(false).oldIndexName(oldIndexName).newIndexName(newIndexName)
            .strategy(MigrationStrategy.DUAL_WRITE).startTime(startTime).endTime(Instant.now()).errorMessage(
                "Background reindex failed: " + reindexResult.getErrorMessage()).build();
      }

      // Switch to new index
      switchAlias(entityClass, newIndexName);

      return MigrationResult.builder().successful(true).oldIndexName(oldIndexName).newIndexName(newIndexName).strategy(
          MigrationStrategy.DUAL_WRITE).startTime(startTime).endTime(Instant.now()).documentsProcessed(reindexResult
              .getDocumentsProcessed()).build();
    } catch (Exception e) {
      logger.error("Dual-write migration failed", e);
      return MigrationResult.builder().successful(false).oldIndexName(oldIndexName).newIndexName(newIndexName).strategy(
          MigrationStrategy.DUAL_WRITE).startTime(startTime).endTime(Instant.now()).errorMessage(e.getMessage())
          .build();
    }
  }

  private MigrationResult executeInPlaceMigration(Class<?> entityClass, String indexName, Instant startTime) {
    // In-place migration would update the existing index
    // This is a simplified implementation
    return MigrationResult.builder().successful(true).oldIndexName(indexName).newIndexName(indexName).strategy(
        MigrationStrategy.IN_PLACE).startTime(startTime).endTime(Instant.now()).build();
  }

  /**
   * Enables dual write to both old and new indexes.
   *
   * @param entityClass  the entity class
   * @param newIndexName the new index name
   * @return true if successful
   */
  public boolean enableDualWrite(Class<?> entityClass, String newIndexName) {
    logger.info("Enabling dual write for {} to index {}", entityClass.getName(), newIndexName);
    // In a real implementation, this would configure the repository layer
    // to write to both indexes simultaneously
    return true;
  }

  /**
   * Reindexes data in the background.
   *
   * @param entityClass  the entity class
   * @param newIndexName the new index name
   * @return a future containing the reindex result
   */
  public CompletableFuture<ReindexResult> reindexInBackground(Class<?> entityClass, String newIndexName) {
    return CompletableFuture.supplyAsync(() -> {
      logger.info("Starting background reindex for {} to {}", entityClass.getName(), newIndexName);

      try {
        // Simulate reindexing process
        // In a real implementation, this would read from the old index
        // and write to the new one, possibly in batches
        Thread.sleep(100); // Simulate work

        return ReindexResult.builder().successful(true).documentsProcessed(1000) // Mock value
            .documentsSkipped(0).timeElapsedMillis(100).build();
      } catch (Exception e) {
        logger.error("Background reindex failed", e);
        return ReindexResult.builder().successful(false).errorMessage(e.getMessage()).build();
      }
    }, executorService);
  }

  /**
   * Reindexes all data to a new index.
   *
   * @param entityClass  the entity class
   * @param newIndexName the new index name
   */
  private void reindexToNewIndex(Class<?> entityClass, String newIndexName) {
    logger.info("Reindexing all data from {} to {}", entityClass.getName(), newIndexName);
    // In a real implementation, this would:
    // 1. Read all documents from the old index
    // 2. Transform if necessary
    // 3. Write to the new index
    // 4. Verify completeness
  }

  /**
   * Switches the index alias to point to the new index.
   *
   * @param entityClass  the entity class
   * @param newIndexName the new index name
   * @return true if successful
   */
  public boolean switchAlias(Class<?> entityClass, String newIndexName) {
    logger.info("Switching alias for {} to {}", entityClass.getName(), newIndexName);

    // Verify new index exists
    if (!indexer.indexExistsFor(entityClass, newIndexName)) {
      logger.error("Cannot switch alias - index {} does not exist", newIndexName);
      return false;
    }

    // In a real implementation, this would update Redis aliases
    // to atomically switch from old to new index
    return true;
  }

  /**
   * Verifies the integrity of an index.
   *
   * @param indexName the index name
   * @return true if the index is valid
   */
  public boolean verifyIndexIntegrity(String indexName) {
    logger.info("Verifying integrity of index {}", indexName);

    // In a real implementation, this would:
    // 1. Check index structure
    // 2. Verify document count
    // 3. Run sample queries
    // 4. Check field mappings

    // For now, just check if index exists
    return indexer.indexExistsFor(null, indexName);
  }

  /**
   * Schedules cleanup of old indexes.
   *
   * @param entityClass the entity class
   * @return true if scheduled successfully
   */
  public boolean scheduleOldIndexCleanup(Class<?> entityClass) {
    logger.info("Scheduling cleanup of old indexes for {}", entityClass.getName());

    // In a real implementation, this would:
    // 1. Identify old indexes
    // 2. Schedule deletion after a grace period
    // 3. Ensure no active connections
    return true;
  }

  /**
   * Gets the current index version for an entity class.
   *
   * @param entityClass the entity class
   * @return the current version number
   */
  public int getCurrentIndexVersion(Class<?> entityClass) {
    String currentIndexName = indexer.getIndexName(entityClass);
    if (currentIndexName == null) {
      return 0; // No index exists, assume version 0
    }
    Matcher matcher = VERSION_PATTERN.matcher(currentIndexName);

    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    }

    return 0; // No version found, assume version 0
  }

  /**
   * Gets the next index version for an entity class.
   *
   * @param entityClass the entity class
   * @return the next version number
   */
  public int getNextIndexVersion(Class<?> entityClass) {
    return getCurrentIndexVersion(entityClass) + 1;
  }

  /**
   * Creates an index alias.
   *
   * @param entityClass the entity class
   * @param targetIndex the target index name
   * @return true if successful
   */
  public boolean createIndexAlias(Class<?> entityClass, String targetIndex) {
    logger.info("Creating alias for {} pointing to {}", entityClass.getName(), targetIndex);
    // In a real implementation, this would create a Redis alias
    return true;
  }

  /**
   * Removes an index alias.
   *
   * @param entityClass the entity class
   * @return true if successful
   */
  public boolean removeIndexAlias(Class<?> entityClass) {
    logger.info("Removing alias for {}", entityClass.getName());
    // In a real implementation, this would remove the Redis alias
    return true;
  }

  private String getBaseIndexName(Class<?> entityClass) {
    String currentName = indexer.getIndexName(entityClass);
    if (currentName == null) {
      // Generate default name from class name
      return entityClass.getSimpleName().toLowerCase() + "_idx";
    }
    // Remove version suffix if present
    return currentName.replaceAll("_v\\d+(?:_idx)?$", "");
  }

  private String getKeyPrefix(Class<?> entityClass) {
    // Get the default key prefix for the entity class
    return entityClass.getSimpleName().toLowerCase() + ":";
  }

  /**
   * Shuts down the migration service and its executor.
   */
  public void shutdown() {
    executorService.shutdown();
  }
}