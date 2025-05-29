package com.redis.om.spring.indexing;

/**
 * Enumeration of vector data types supported for vector indexing in Redis OM Spring.
 * This enum defines the floating-point precision options available when storing
 * and indexing vector embeddings.
 * 
 * <p>Vector type selection impacts:</p>
 * <ul>
 * <li><strong>Storage efficiency</strong>: FLOAT32 uses 4 bytes per dimension, FLOAT64 uses 8 bytes</li>
 * <li><strong>Precision</strong>: FLOAT64 provides higher precision but at the cost of memory</li>
 * <li><strong>Performance</strong>: FLOAT32 operations are generally faster</li>
 * </ul>
 * 
 * <p>Typical usage:</p>
 * <ul>
 * <li>FLOAT32 is recommended for most machine learning embeddings (default for most models)</li>
 * <li>FLOAT64 is used when higher precision is required or when working with scientific data</li>
 * </ul>
 * 
 * @since 1.0
 * @see com.redis.om.spring.annotations.VectorIndexed
 * @see com.redis.om.spring.indexing.DistanceMetric
 */
public enum VectorType {
  /** 32-bit floating point vector type (single precision) */
  FLOAT32,

  /** 64-bit floating point vector type (double precision) */
  FLOAT64
}
