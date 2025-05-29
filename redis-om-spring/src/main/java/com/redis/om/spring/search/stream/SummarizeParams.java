package com.redis.om.spring.search.stream;

/**
 * Configuration parameters for search result summarization in Redis OM Spring.
 * 
 * <p>This class provides a fluent API for configuring how search results are
 * summarized when using full-text search features. Summarization extracts relevant
 * text fragments from matching documents to provide context around search terms.</p>
 * 
 * <p>The summarization process highlights matching terms within document fields
 * and returns text fragments that show where and how the search terms appear
 * in the content.</p>
 * 
 * <p>Default configuration:</p>
 * <ul>
 * <li>Number of fragments: 3</li>
 * <li>Fragment size: 20 tokens</li>
 * <li>Fragment separator: "..."</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Use default summarization parameters
 * var summary = entityStream
 * .filter(Article$.CONTENT.containing("redis"))
 * .summarize(SummarizeParams.instance());
 * 
 * // Customize summarization
 * var customSummary = entityStream
 * .filter(Article$.CONTENT.containing("database"))
 * .summarize(SummarizeParams.instance()
 * .fragments(5) // Get 5 fragments
 * .size(50) // Each fragment has 50 tokens
 * .separator(" | ")); // Use pipe as separator
 * </pre>
 * 
 * @since 1.0
 * @see com.redis.om.spring.search.stream.EntityStream
 * @see com.redis.om.spring.search.stream.SearchStream
 */
public class SummarizeParams {
  /** The number of text fragments to return per field */
  private Integer fragsNum = 3;

  /** The size of each fragment in tokens */
  private Integer fragSize = 20;

  /** The separator string between fragments */
  private String separator = "...";

  /**
   * Default constructor for SummarizeParams.
   * <p>
   * Creates a new instance with default summarization parameters.
   * This constructor is used internally and by the factory methods.
   */
  public SummarizeParams() {
    // Default constructor with default values
  }

  /**
   * Creates a new instance with default parameters.
   * 
   * @return a new SummarizeParams instance with default values
   */
  public static SummarizeParams instance() {
    return new SummarizeParams();
  }

  /**
   * Returns the number of fragments to extract.
   * 
   * @return the number of fragments
   */
  public Integer getFragsNum() {
    return fragsNum;
  }

  /**
   * Returns the size of each fragment in tokens.
   * 
   * @return the fragment size
   */
  public Integer getFragSize() {
    return fragSize;
  }

  /**
   * Returns the separator string used between fragments.
   * 
   * @return the fragment separator
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * Sets the number of fragments to extract from matching documents.
   * 
   * <p>More fragments provide more context but may result in longer summaries.
   * The optimal number depends on your use case and UI constraints.</p>
   * 
   * @param num the number of fragments to extract (must be positive)
   * @return this instance for method chaining
   */
  public SummarizeParams fragments(int num) {
    this.fragsNum = num;
    return this;
  }

  /**
   * Sets the size of each fragment in tokens.
   * 
   * <p>Larger fragments provide more context around matching terms but may
   * include less relevant content. Smaller fragments are more focused but
   * may lack context.</p>
   * 
   * @param size the fragment size in tokens (must be positive)
   * @return this instance for method chaining
   */
  public SummarizeParams size(int size) {
    this.fragSize = size;
    return this;
  }

  /**
   * Sets the separator string between fragments.
   * 
   * <p>The separator helps visually distinguish between different text fragments
   * in the summarized output. Common separators include "...", " | ", or " /// ".</p>
   * 
   * @param separator the separator string (must not be null)
   * @return this instance for method chaining
   */
  public SummarizeParams separator(String separator) {
    this.separator = separator;
    return this;
  }
}
