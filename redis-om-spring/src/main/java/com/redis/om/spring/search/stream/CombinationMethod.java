package com.redis.om.spring.search.stream;

/**
 * Defines the score combination method for hybrid search queries.
 * <p>
 * When performing hybrid search (combining text and vector similarity),
 * the scores from each search type need to be combined. This enum controls
 * which algorithm is used for that combination.
 * </p>
 *
 * @since 2.1.0
 */
public enum CombinationMethod {

  /**
   * Reciprocal Rank Fusion - combines results based on their rank positions
   * rather than raw scores. Requires Redis 8.4+ for native FT.HYBRID support;
   * falls back to LINEAR via FT.AGGREGATE on older versions.
   */
  RRF,

  /**
   * Weighted linear combination of text and vector scores.
   * Uses the formula: {@code hybrid_score = (1-alpha) * text_score + alpha * vector_similarity}
   */
  LINEAR
}
