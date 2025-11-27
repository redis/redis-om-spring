package com.redis.om.spring.indexing;

/**
 * Defines the strategy for migrating Redis indexes.
 *
 * @since 1.0.0
 */
public enum MigrationStrategy {
  /**
   * Blue-Green deployment strategy.
   * Creates a new index, reindexes all data, then atomically switches aliases.
   * Provides zero-downtime migration with atomic switchover.
   */
  BLUE_GREEN,

  /**
   * Dual-write strategy.
   * Writes to both old and new indexes simultaneously during migration.
   * Allows gradual migration with eventual consistency.
   */
  DUAL_WRITE,

  /**
   * In-place update strategy.
   * Updates the existing index structure without creating a new one.
   * May cause brief unavailability during schema changes.
   */
  IN_PLACE
}