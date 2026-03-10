package com.redis.om.spring.search.stream;

/**
 * Defines the scoring algorithm used by Redis FT.SEARCH to rank results.
 * <p>
 * Each scorer implements a different relevance ranking strategy. The default
 * scorer used by Redis is TFIDF. Set a scorer on a {@link SearchStream} via
 * {@link SearchStream#scorer(Scorer)}.
 * </p>
 *
 * @since 2.1.0
 * @see SearchStream#scorer(Scorer)
 * @see SearchStream#withScores()
 */
public enum Scorer {

  /** Term Frequency-Inverse Document Frequency (default). */
  TFIDF("TFIDF"),

  /** TF-IDF with document-length normalization. */
  TFIDF_DOCNORM("TFIDF.DOCNORM"),

  /** BM25 standard scoring. */
  BM25STD("BM25STD"),

  /** BM25 standard with document-length normalization. */
  BM25STD_NORM("BM25STD.NORM"),

  /** BM25 standard with hyperbolic tangent normalization. */
  BM25STD_TANH("BM25STD.TANH"),

  /** Maximum of per-term scores. */
  DISMAX("DISMAX"),

  /** Uses the document's static score (set at index time). */
  DOCSCORE("DOCSCORE"),

  /** Hamming distance scorer (for binary data). */
  HAMMING("HAMMING");

  private final String value;

  Scorer(String value) {
    this.value = value;
  }

  /**
   * Returns the Redis protocol string for this scorer.
   *
   * @return the scorer name as expected by FT.SEARCH SCORER parameter
   */
  public String getValue() {
    return value;
  }
}
