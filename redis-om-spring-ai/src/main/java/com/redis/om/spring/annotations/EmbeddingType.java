package com.redis.om.spring.annotations;

/**
 * Enumeration of embedding types supported by Redis OM Spring for vectorization.
 * <p>
 * This enum defines the different types of data that can be converted into vector embeddings
 * for storage and similarity search in Redis. The embedding type influences the choice of
 * model and processing approach used during vectorization.
 * </p>
 * 
 * @see Vectorize
 * @see EmbeddingProvider
 * @see com.redis.om.spring.vectorize.Embedder
 * @since 1.0.0
 */
public enum EmbeddingType {
  /**
   * Image embedding type for converting images into vector representations.
   * Used for image similarity search and visual content retrieval.
   */
  IMAGE,
  /**
   * Sentence embedding type for converting complete sentences or paragraphs into vectors.
   * Optimized for capturing semantic meaning of longer text passages.
   * This is the default embedding type for text content.
   */
  SENTENCE,
  /**
   * Word embedding type for converting individual words or short phrases into vectors.
   * Suitable for word-level similarity and lexical analysis.
   */
  WORD,
  /**
   * Face embedding type for converting facial features into vector representations.
   * Used for face recognition, similarity matching, and facial analysis tasks.
   */
  FACE
}
