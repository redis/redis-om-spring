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

  public boolean isSuccessful() {
    return successful;
  }

  public String getOldIndexName() {
    return oldIndexName;
  }

  public String getNewIndexName() {
    return newIndexName;
  }

  public MigrationStrategy getStrategy() {
    return strategy;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public long getDocumentsProcessed() {
    return documentsProcessed;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean successful;
    private String oldIndexName;
    private String newIndexName;
    private MigrationStrategy strategy;
    private Instant startTime;
    private Instant endTime;
    private String errorMessage;
    private long documentsProcessed;

    public Builder successful(boolean successful) {
      this.successful = successful;
      return this;
    }

    public Builder oldIndexName(String oldIndexName) {
      this.oldIndexName = oldIndexName;
      return this;
    }

    public Builder newIndexName(String newIndexName) {
      this.newIndexName = newIndexName;
      return this;
    }

    public Builder strategy(MigrationStrategy strategy) {
      this.strategy = strategy;
      return this;
    }

    public Builder startTime(Instant startTime) {
      this.startTime = startTime;
      return this;
    }

    public Builder endTime(Instant endTime) {
      this.endTime = endTime;
      return this;
    }

    public Builder errorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public Builder documentsProcessed(long documentsProcessed) {
      this.documentsProcessed = documentsProcessed;
      return this;
    }

    public MigrationResult build() {
      return new MigrationResult(this);
    }
  }
}