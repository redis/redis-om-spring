package com.redis.om.spring.autocomplete;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an autocomplete suggestion in Redis OM Spring.
 * 
 * <p>This class encapsulates a suggestion value along with its associated score
 * and optional payload data. Suggestions are used with the {@code @AutoComplete}
 * annotation to provide real-time autocomplete functionality for text fields.</p>
 * 
 * <p>Each suggestion consists of:</p>
 * <ul>
 * <li>A value - the actual text of the suggestion</li>
 * <li>A score - numerical weight used for ranking (default: 1.0)</li>
 * <li>A payload - optional map of additional data associated with the suggestion</li>
 * </ul>
 * 
 * <p>Suggestions are stored in Redis using special key formats that include the
 * entity class and field name for efficient retrieval and management.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Create a simple suggestion
 * Suggestion simple = new Suggestion("apple");
 * 
 * // Create a suggestion with score
 * Suggestion scored = new Suggestion("apple pie", 2.5);
 * 
 * // Create a suggestion with payload
 * Map&lt;String, Object&gt; metadata = Map.of("category", "dessert", "calories", 300);
 * Suggestion full = new Suggestion("apple pie", 2.5, metadata);
 * </pre>
 * 
 * @since 1.0
 * @see com.redis.om.spring.annotations.AutoComplete
 * @see com.redis.om.spring.annotations.AutoCompletePayload
 */
public class Suggestion {
  /** Redis key format for storing suggestion values: "sugg:{class}:{field}" */
  public static final String KEY_FORMAT_STRING = "sugg:%s:%s";

  /** Redis key format for storing suggestion payloads: "sugg:payload:{class}:{field}" */
  public static final String PAYLOAD_KEY_FORMAT_STRING = "sugg:payload:%s:%s";

  /** The text value of the suggestion */
  private final String value;

  /** The score used for ranking suggestions (higher scores rank first) */
  private double score = 1.0;

  /** Optional payload data associated with the suggestion */
  private Map<String, Object> payload = new HashMap<>();

  /**
   * Creates a suggestion with all properties specified.
   * 
   * @param value   the text value of the suggestion
   * @param score   the score for ranking (must be positive)
   * @param payload additional data associated with the suggestion
   */
  public Suggestion(String value, double score, Map<String, Object> payload) {
    this.value = value;
    this.payload = payload;
    this.score = score;
  }

  /**
   * Creates a suggestion with a value and score.
   * 
   * @param value the text value of the suggestion
   * @param score the score for ranking (must be positive)
   */
  public Suggestion(String value, double score) {
    this.value = value;
    this.score = score;
  }

  /**
   * Creates a suggestion with a value and payload.
   * The score defaults to 1.0.
   * 
   * @param value   the text value of the suggestion
   * @param payload additional data associated with the suggestion
   */
  public Suggestion(String value, Map<String, Object> payload) {
    this.value = value;
    this.payload = payload;
  }

  /**
   * Creates a suggestion with only a value.
   * The score defaults to 1.0 and payload is empty.
   * 
   * @param value the text value of the suggestion
   */
  public Suggestion(String value) {
    this.value = value;
  }

  /**
   * Returns the text value of this suggestion.
   * 
   * @return the suggestion text
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the score of this suggestion.
   * Higher scores result in higher ranking in autocomplete results.
   * 
   * @return the suggestion score
   */
  public double getScore() {
    return score;
  }

  /**
   * Returns the payload data associated with this suggestion.
   * The payload can contain any additional metadata about the suggestion.
   * 
   * @return the suggestion payload, never null but may be empty
   */
  public Map<String, Object> getPayload() {
    return payload;
  }
}
