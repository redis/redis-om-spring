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

  /**
   * Returns whether the reindexing was successful.
   *
   * @return true if the reindexing succeeded, false otherwise
   */
  public boolean isSuccessful() {
    return successful;
  }

  /**
   * Returns the number of documents processed during reindexing.
   *
   * @return the number of documents processed
   */
  public long getDocumentsProcessed() {
    return documentsProcessed;
  }

  /**
   * Returns the number of documents skipped during reindexing.
   *
   * @return the number of documents skipped
   */
  public long getDocumentsSkipped() {
    return documentsSkipped;
  }

  /**
   * Returns the time elapsed in milliseconds for the reindexing operation.
   *
   * @return the time elapsed in milliseconds
   */
  public long getTimeElapsedMillis() {
    return timeElapsedMillis;
  }

  /**
   * Returns the error message if the reindexing failed.
   *
   * @return the error message, or null if successful
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Creates a new Builder for constructing ReindexResult instances.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for constructing ReindexResult instances.
   */
  public static class Builder {
    private boolean successful;
    private long documentsProcessed;
    private long documentsSkipped;
    private long timeElapsedMillis;
    private String errorMessage;

    /**
     * Creates a new Builder instance.
     */
    public Builder() {
      // Default constructor
    }

    /**
     * Sets whether the reindexing was successful.
     *
     * @param successful true if successful, false otherwise
     * @return this builder for chaining
     */
    public Builder successful(boolean successful) {
      this.successful = successful;
      return this;
    }

    /**
     * Sets the number of documents processed.
     *
     * @param documentsProcessed the number of documents processed
     * @return this builder for chaining
     */
    public Builder documentsProcessed(long documentsProcessed) {
      this.documentsProcessed = documentsProcessed;
      return this;
    }

    /**
     * Sets the number of documents skipped.
     *
     * @param documentsSkipped the number of documents skipped
     * @return this builder for chaining
     */
    public Builder documentsSkipped(long documentsSkipped) {
      this.documentsSkipped = documentsSkipped;
      return this;
    }

    /**
     * Sets the time elapsed in milliseconds.
     *
     * @param timeElapsedMillis the time elapsed in milliseconds
     * @return this builder for chaining
     */
    public Builder timeElapsedMillis(long timeElapsedMillis) {
      this.timeElapsedMillis = timeElapsedMillis;
      return this;
    }

    /**
     * Sets the error message for failed reindexing.
     *
     * @param errorMessage the error message
     * @return this builder for chaining
     */
    public Builder errorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    /**
     * Builds the ReindexResult instance.
     *
     * @return a new ReindexResult with the configured values
     */
    public ReindexResult build() {
      return new ReindexResult(this);
    }
  }
}
