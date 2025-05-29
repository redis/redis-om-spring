package com.redis.om.spring.indexing;

/**
 * Enumeration of distance metrics supported for vector similarity search.
 * This enum defines the different distance calculation methods that can be used
 * when performing vector similarity searches with Redis OM Spring.
 * 
 * <p>Each metric calculates similarity differently:</p>
 * <ul>
 * <li><strong>L2</strong>: Euclidean distance - measures straight-line distance in vector space</li>
 * <li><strong>IP</strong>: Inner Product - calculates dot product similarity (higher values = more similar)</li>
 * <li><strong>COSINE</strong>: Cosine similarity - measures angle between vectors (ignores magnitude)</li>
 * </ul>
 * 
 * <p>The choice of distance metric affects search results and performance:
 * <ul>
 * <li>L2 is good for spatial data and when magnitude matters</li>
 * <li>IP is efficient for normalized vectors</li>
 * <li>COSINE is ideal for text embeddings and when direction matters more than magnitude</li>
 * </ul>
 * 
 * @since 1.0
 * @see com.redis.om.spring.annotations.VectorIndexed
 */
public enum DistanceMetric {
  /** Euclidean (L2) distance metric */
  L2,

  /** Inner Product distance metric */
  IP,

  /** Cosine similarity distance metric */
  COSINE
}
