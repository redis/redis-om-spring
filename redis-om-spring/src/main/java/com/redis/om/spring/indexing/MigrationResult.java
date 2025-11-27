package com.redis.om.spring.indexing;

import java.time.Instant;

/**
 * Represents the result of an index migration operation.
 *
 * @since 1.0.0
 */
public class MigrationResult {
  private final boolean successful;
  private final String oldIndexName;
  private final String newIndexName;
  private final MigrationStrategy strategy;
  private final Instant startTime;
  private final Instant endTime;
  private final String errorMessage;
  private final long documentsProcessed;

  private MigrationResult(Builder builder) {
    this.successful = builder.successful;
    this.oldIndexName = builder.oldIndexName;
    this.newIndexName = builder.newIndexName;
    this.strategy = builder.strategy;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.errorMessage = builder.errorMessage;
    this.documentsProcessed = builder.documentsProcessed;
  }

  /**
   * Returns whether the migration was successful.
   *
   * @return true if the migration succeeded, false otherwise
   */
  public boolean isSuccessful() {
    return successful;
  }

  /**
   * Returns the name of the old index before migration.
   *
   * @return the old index name
   */
  public String getOldIndexName() {
    return oldIndexName;
  }

  /**
   * Returns the name of the new index after migration.
   *
   * @return the new index name
   */
  public String getNewIndexName() {
    return newIndexName;
  }

  /**
   * Returns the migration strategy used.
   *
   * @return the migration strategy
   */
  public MigrationStrategy getStrategy() {
    return strategy;
  }

  /**
   * Returns the start time of the migration.
   *
   * @return the start time
   */
  public Instant getStartTime() {
    return startTime;
  }

  /**
   * Returns the end time of the migration.
   *
   * @return the end time
   */
  public Instant getEndTime() {
    return endTime;
  }

  /**
   * Returns the error message if the migration failed.
   *
   * @return the error message, or null if successful
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Returns the number of documents processed during migration.
   *
   * @return the number of documents processed
   */
  public long getDocumentsProcessed() {
    return documentsProcessed;
  }

  /**
   * Creates a new Builder for constructing MigrationResult instances.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for constructing MigrationResult instances.
   */
  public static class Builder {
    private boolean successful;
    private String oldIndexName;
    private String newIndexName;
    private MigrationStrategy strategy;
    private Instant startTime;
    private Instant endTime;
    private String errorMessage;
    private long documentsProcessed;

    /**
     * Creates a new Builder instance.
     */
    public Builder() {
      // Default constructor
    }

    /**
     * Sets whether the migration was successful.
     *
     * @param successful true if successful, false otherwise
     * @return this builder for chaining
     */
    public Builder successful(boolean successful) {
      this.successful = successful;
      return this;
    }

    /**
     * Sets the old index name.
     *
     * @param oldIndexName the name of the old index
     * @return this builder for chaining
     */
    public Builder oldIndexName(String oldIndexName) {
      this.oldIndexName = oldIndexName;
      return this;
    }

    /**
     * Sets the new index name.
     *
     * @param newIndexName the name of the new index
     * @return this builder for chaining
     */
    public Builder newIndexName(String newIndexName) {
      this.newIndexName = newIndexName;
      return this;
    }

    /**
     * Sets the migration strategy used.
     *
     * @param strategy the migration strategy
     * @return this builder for chaining
     */
    public Builder strategy(MigrationStrategy strategy) {
      this.strategy = strategy;
      return this;
    }

    /**
     * Sets the start time of the migration.
     *
     * @param startTime the start time
     * @return this builder for chaining
     */
    public Builder startTime(Instant startTime) {
      this.startTime = startTime;
      return this;
    }

    /**
     * Sets the end time of the migration.
     *
     * @param endTime the end time
     * @return this builder for chaining
     */
    public Builder endTime(Instant endTime) {
      this.endTime = endTime;
      return this;
    }

    /**
     * Sets the error message for failed migrations.
     *
     * @param errorMessage the error message
     * @return this builder for chaining
     */
    public Builder errorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    /**
     * Sets the number of documents processed.
     *
     * @param documentsProcessed the number of documents
     * @return this builder for chaining
     */
    public Builder documentsProcessed(long documentsProcessed) {
      this.documentsProcessed = documentsProcessed;
      return this;
    }

    /**
     * Builds the MigrationResult instance.
     *
     * @return a new MigrationResult with the configured values
     */
    public MigrationResult build() {
      return new MigrationResult(this);
    }
  }
}
