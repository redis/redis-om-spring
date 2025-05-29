package com.redis.om.spring.annotations;

import java.lang.annotation.*;

import com.redis.om.spring.indexing.DistanceMetric;
import com.redis.om.spring.indexing.VectorType;

import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

/**
 * Annotation to mark a field as a vector field for similarity search indexing.
 * <p>
 * This annotation configures a field (typically a float[] or byte[] array) to be indexed
 * as a vector in RediSearch, enabling k-nearest neighbors (KNN) similarity search.
 * Vector fields are commonly used to store embeddings from machine learning models
 * for semantic search, recommendation systems, and similarity-based retrieval.
 * </p>
 * <p>
 * Key concepts in vector similarity search:
 * <ul>
 * <li><b>Embeddings:</b> Dense numerical representations of data (text, images, etc.)</li>
 * <li><b>Distance Metrics:</b> Mathematical functions to measure similarity between vectors</li>
 * <li><b>Indexing Algorithms:</b> Data structures optimized for fast similarity search</li>
 * <li><b>k-NN Search:</b> Finding the k most similar vectors to a query vector</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Document
 * public class Product {
 * 
 * @Id
 *     private String id;
 * 
 * @Indexed
 *          private String name;
 * 
 *          @VectorIndexed(
 *          algorithm = VectorAlgorithm.HNSW,
 *          dimension = 768,
 *          distanceMetric = DistanceMetric.COSINE,
 *          initialCapacity = 10000
 *          )
 *          private float[] descriptionEmbedding;
 *          }
 *          }</pre>
 * 
 * @see com.redis.om.spring.indexing.DistanceMetric
 * @see com.redis.om.spring.indexing.VectorType
 * @see redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm
 * @since 0.1.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface VectorIndexed {
  /**
   * Custom field name for the vector index.
   * If not specified, the Java field name will be used.
   * 
   * @return the field name in the Redis index
   */
  String fieldName() default "";

  /**
   * Alias for the vector field in search queries.
   * Useful for providing a shorter or more query-friendly name.
   * 
   * @return the field alias
   */
  String alias() default "";

  /**
   * The indexing algorithm to use for vector similarity search.
   * <p>
   * Available algorithms:
   * <ul>
   * <li>{@link VectorAlgorithm#FLAT} - Brute force search, accurate but slower for large datasets</li>
   * <li>{@link VectorAlgorithm#HNSW} - Hierarchical Navigable Small World, approximate but faster</li>
   * </ul>
   * 
   * @return the vector indexing algorithm
   */
  VectorAlgorithm algorithm() default VectorAlgorithm.FLAT;

  /**
   * The data type of the vector components.
   * <p>
   * Supported types:
   * <ul>
   * <li>{@link VectorType#FLOAT32} - 32-bit floating point (default)</li>
   * <li>{@link VectorType#FLOAT64} - 64-bit floating point</li>
   * </ul>
   * 
   * @return the vector component type
   */
  VectorType type() default VectorType.FLOAT32;

  /**
   * The total number of index attributes.
   * <p>
   * This parameter counts all attributes passed to the index command,
   * including algorithm-specific parameters. It is used internally by
   * RediSearch for index configuration validation.
   * </p>
   * 
   * @return the number of index attributes
   * @deprecated This parameter is calculated automatically in most cases
   */
  @Deprecated
  int count() default Integer.MIN_VALUE;

  /**
   * The dimensionality of the vector (required).
   * <p>
   * Must match the actual dimension of vectors stored in this field.
   * Common dimensions include:
   * <ul>
   * <li>384, 768 - Common for text embeddings (e.g., sentence transformers)</li>
   * <li>512, 2048 - Common for image embeddings</li>
   * <li>1536 - OpenAI text-embedding-ada-002 dimension</li>
   * </ul>
   * 
   * @return the vector dimension as a positive integer
   */
  int dimension() default Integer.MIN_VALUE;

  /**
   * The distance metric for measuring vector similarity.
   * <p>
   * Available metrics:
   * <ul>
   * <li>{@link DistanceMetric#L2} - Euclidean distance (default)</li>
   * <li>{@link DistanceMetric#IP} - Inner product (dot product)</li>
   * <li>{@link DistanceMetric#COSINE} - Cosine similarity</li>
   * </ul>
   * Choose based on your embedding model and use case.
   * 
   * @return the distance metric
   */
  DistanceMetric distanceMetric() default DistanceMetric.L2;

  /**
   * Initial capacity for vector storage.
   * <p>
   * Pre-allocates memory for the specified number of vectors,
   * improving performance when the expected dataset size is known.
   * The index can grow beyond this size but may require reallocation.
   * </p>
   * 
   * @return the initial vector capacity
   */
  int initialCapacity() default Integer.MIN_VALUE;

  // FLAT Algorithm Parameters

  /**
   * Block size for FLAT algorithm (FLAT algorithm only).
   * <p>
   * Vectors are stored in blocks of this size. Larger blocks improve
   * query performance but increase memory usage. Useful for dynamic
   * indexes with frequent additions and deletions.
   * </p>
   * 
   * @return the block size (default: 1024)
   */
  int blockSize() default 1024;

  // HNSW Algorithm Parameters

  /**
   * Number of bi-directional links per node (HNSW algorithm only).
   * <p>
   * Controls the connectivity of the HNSW graph. Higher values increase
   * accuracy and query time but also memory usage. On layer 0, each node
   * has up to 2*M connections.
   * </p>
   * 
   * @return the M parameter (default: 16)
   */
  int m() default 16;

  /**
   * Size of the dynamic candidate list during graph construction (HNSW algorithm only).
   * <p>
   * Higher values create a more accurate graph structure but increase
   * indexing time. This parameter affects index build time, not query time.
   * </p>
   * 
   * @return the efConstruction parameter (default: 200)
   */
  int efConstruction() default 200;

  /**
   * Size of the dynamic candidate list during search (HNSW algorithm only).
   * <p>
   * Higher values increase search accuracy at the cost of query time.
   * This parameter can be overridden per query for fine-tuning the
   * accuracy/performance trade-off.
   * </p>
   * 
   * @return the efRuntime parameter (default: 10)
   */
  int efRuntime() default 10;

  /**
   * Relative factor for range query boundaries (HNSW algorithm only).
   * <p>
   * In range queries, vectors within distance * (1 + epsilon) are considered
   * as candidates. Higher values increase accuracy but also query time.
   * </p>
   * 
   * @return the epsilon parameter (default: 0.01)
   */
  double epsilon() default 0.01;
}