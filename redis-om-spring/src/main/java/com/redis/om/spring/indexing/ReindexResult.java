package com.redis.om.spring.indexing;

/**
 * Represents the result of a reindexing operation.
 *
 * @since 1.0.0
 */
public class ReindexResult {
  private final boolean successful;
  private final long documentsProcessed;
  private final long documentsSkipped;
  private final long timeElapsedMillis;
  private final String errorMessage;

  private ReindexResult(Builder builder) {
    this.successful = builder.successful;
    this.documentsProcessed = builder.documentsProcessed;
    this.documentsSkipped = builder.documentsSkipped;
    this.timeElapsedMillis = builder.timeElapsedMillis;
    this.errorMessage = builder.errorMessage;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public long getDocumentsProcessed() {
    return documentsProcessed;
  }

  public long getDocumentsSkipped() {
    return documentsSkipped;
  }

  public long getTimeElapsedMillis() {
    return timeElapsedMillis;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean successful;
    private long documentsProcessed;
    private long documentsSkipped;
    private long timeElapsedMillis;
    private String errorMessage;

    public Builder successful(boolean successful) {
      this.successful = successful;
      return this;
    }

    public Builder documentsProcessed(long documentsProcessed) {
      this.documentsProcessed = documentsProcessed;
      return this;
    }

    public Builder documentsSkipped(long documentsSkipped) {
      this.documentsSkipped = documentsSkipped;
      return this;
    }

    public Builder timeElapsedMillis(long timeElapsedMillis) {
      this.timeElapsedMillis = timeElapsedMillis;
      return this;
    }

    public Builder errorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public ReindexResult build() {
      return new ReindexResult(this);
    }
  }
}