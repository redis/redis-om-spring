package com.redis.om.spring.repository.query.autocomplete;

/**
 * Configuration options for Redis autocomplete operations.
 * This class provides a fluent API for configuring autocomplete queries
 * with various options including fuzzy matching, scoring, payloads, and limits.
 * 
 * <p>AutoCompleteOptions supports method chaining for convenient configuration:</p>
 * <pre>{@code
 * AutoCompleteOptions options = AutoCompleteOptions.get()
 *     .fuzzy()
 *     .withScore()
 *     .withPayload()
 *     .limit(10);
 * }</pre>
 * 
 * @since 1.0
 * @see com.redis.om.spring.annotations.AutoComplete
 */
public class AutoCompleteOptions {
  private boolean fuzzy = false;
  private int limit = 5;
  private boolean withScore = false;
  private boolean withPayload = false;

  /**
   * Creates a new AutoCompleteOptions with default settings.
   */
  public AutoCompleteOptions() {
  }

  /**
   * Creates a new AutoCompleteOptions instance.
   * This is a factory method for convenient instantiation.
   * 
   * @return a new AutoCompleteOptions with default settings
   */
  public static AutoCompleteOptions get() {
    return new AutoCompleteOptions();
  }

  /**
   * Enables payload inclusion in autocomplete results.
   * 
   * @return this AutoCompleteOptions instance for method chaining
   */
  public AutoCompleteOptions withPayload() {
    setWithPayload(true);
    return this;
  }

  /**
   * Enables score inclusion in autocomplete results.
   * 
   * @return this AutoCompleteOptions instance for method chaining
   */
  public AutoCompleteOptions withScore() {
    setWithScore(true);
    return this;
  }

  /**
   * Sets the maximum number of results to return.
   * 
   * @param limit the maximum number of results
   * @return this AutoCompleteOptions instance for method chaining
   */
  public AutoCompleteOptions limit(Integer limit) {
    setLimit(limit);
    return this;
  }

  /**
   * Enables fuzzy matching for autocomplete queries.
   * Fuzzy matching allows for approximate string matching with typos.
   * 
   * @return this AutoCompleteOptions instance for method chaining
   */
  public AutoCompleteOptions fuzzy() {
    setFuzzy(true);
    return this;
  }

  /**
   * Returns whether fuzzy matching is enabled.
   * 
   * @return true if fuzzy matching is enabled
   */
  public boolean isFuzzy() {
    return fuzzy;
  }

  /**
   * Sets whether fuzzy matching is enabled.
   * 
   * @param fuzzy true to enable fuzzy matching
   */
  public void setFuzzy(boolean fuzzy) {
    this.fuzzy = fuzzy;
  }

  /**
   * Returns whether scores should be included in results.
   * 
   * @return true if scores should be included
   */
  public boolean isWithScore() {
    return withScore;
  }

  /**
   * Sets whether scores should be included in results.
   * 
   * @param withScore true to include scores
   */
  public void setWithScore(boolean withScore) {
    this.withScore = withScore;
  }

  /**
   * Returns whether payloads should be included in results.
   * 
   * @return true if payloads should be included
   */
  public boolean isWithPayload() {
    return withPayload;
  }

  /**
   * Sets whether payloads should be included in results.
   * 
   * @param withPayload true to include payloads
   */
  public void setWithPayload(boolean withPayload) {
    this.withPayload = withPayload;
  }

  /**
   * Returns the maximum number of results to return.
   * 
   * @return the result limit
   */
  public int getLimit() {
    return limit;
  }

  /**
   * Sets the maximum number of results to return.
   * 
   * @param limit the result limit
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }
}
