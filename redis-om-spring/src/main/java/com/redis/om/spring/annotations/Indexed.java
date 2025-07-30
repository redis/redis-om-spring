package com.redis.om.spring.annotations;

import java.lang.annotation.*;

import com.redis.om.spring.indexing.DistanceMetric;
import com.redis.om.spring.indexing.VectorType;

import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

/**
 * Annotation to mark fields for indexing in Redis with comprehensive search capabilities.
 * <p>
 * This annotation enables various types of search indexes including text, numeric, tag,
 * geospatial, and vector indexes. It provides extensive configuration options for
 * different field types and search requirements.
 * </p>
 * 
 * @since 1.0.0
 * @see SchemaFieldType
 * @see SerializationHint
 * @see VectorAlgorithm
 * @see DistanceMetric
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface Indexed {
  /**
   * Specifies the schema field type for indexing.
   * By default, attempts to determine the schema field type from the Java datatype.
   * 
   * @return the schema field type
   */
  SchemaFieldType schemaFieldType() default SchemaFieldType.AUTODETECT;

  /**
   * Provides hints for field serialization.
   * 
   * @return the serialization hint
   */
  SerializationHint serializationHint() default SerializationHint.NONE;

  /**
   * Specifies the field name to use in the Redis index.
   * If not specified, the field name from the Java class will be used.
   * 
   * @return the field name for indexing
   */
  String fieldName() default "";

  /**
   * Specifies an alias for the field in search queries.
   * 
   * @return the field alias for search queries
   */
  String alias() default "";

  /**
   * Indicates whether the field can be used for sorting search results.
   * 
   * @return {@code true} if the field should be sortable, {@code false} otherwise
   */
  boolean sortable() default false;

  /**
   * Indicates whether the field should be stored but not indexed.
   * 
   * @return {@code true} if the field should not be indexed, {@code false} otherwise
   */
  boolean noindex() default false;

  /**
   * Specifies the weight for text field scoring in search results.
   * 
   * @return the field weight for text scoring
   */
  double weight() default 1.0;

  /**
   * Indicates whether stemming should be disabled for text fields.
   * 
   * @return {@code true} if stemming should be disabled, {@code false} otherwise
   */
  boolean nostem() default false;

  /**
   * Specifies the phonetic algorithm for text fields.
   * 
   * @return the phonetic algorithm name
   */
  String phonetic() default "";

  /**
   * Specifies the separator character for tag fields.
   * 
   * @return the separator character
   */
  String separator() default "|";

  /**
   * Specifies the array index for multi-value fields.
   * 
   * @return the array index
   */
  int arrayIndex() default Integer.MIN_VALUE;

  // -----------------
  // VECTOR properties
  // -----------------

  /**
   * Specifies the vector indexing algorithm.
   * 
   * @return the vector algorithm
   */
  VectorAlgorithm algorithm() default VectorAlgorithm.FLAT;

  /**
   * Specifies the vector type. Current supported types are FLOAT32 and FLOAT64.
   * 
   * @return the vector type
   */
  VectorType type() default VectorType.FLOAT32;

  /**
   * Specifies the number of attributes for the vector index.
   * Counts the total number of attributes passed for the index in the command,
   * although algorithm parameters should be submitted as named arguments.
   * 
   * @return the number of attributes
   */
  int count() default Integer.MIN_VALUE;

  /**
   * Specifies the vector dimension as a positive integer.
   * 
   * @return the vector dimension
   */
  int dimension() default Integer.MIN_VALUE;

  /**
   * Specifies the distance metric for vector similarity search.
   * Supported metrics are L2, IP (Inner Product), and COSINE.
   * 
   * @return the distance metric
   */
  DistanceMetric distanceMetric() default DistanceMetric.L2;

  /**
   * Specifies the initial vector capacity in the index.
   * This affects the memory allocation size of the index.
   * 
   * @return the initial capacity
   */
  int initialCapacity() default Integer.MIN_VALUE;

  /**
   * Specifies the block size for FLAT algorithm.
   * Block size to hold BLOCK_SIZE amount of vectors in a contiguous array.
   * This is useful when the index is dynamic with respect to addition and deletion.
   * 
   * @return the block size (defaults to 1024)
   */
  int blockSize() default 1024;

  /**
   * Specifies the M parameter for HNSW algorithm.
   * Number of maximum allowed outgoing edges for each node in the graph in each layer.
   * On layer zero the maximal number of outgoing edges will be 2M.
   * 
   * @return the M parameter (defaults to 16)
   */
  int m() default 16;

  /**
   * Specifies the EF_CONSTRUCTION parameter for HNSW algorithm.
   * Number of maximum allowed potential outgoing edges candidates for each node
   * in the graph, during the graph building.
   * 
   * @return the EF_CONSTRUCTION parameter (defaults to 200)
   */
  int efConstruction() default 200;

  /**
   * Specifies the EF_RUNTIME parameter for HNSW algorithm.
   * Number of maximum top candidates to hold during the KNN search.
   * Higher values lead to more accurate results at the expense of longer runtime.
   * 
   * @return the EF_RUNTIME parameter (defaults to 10)
   */
  int efRuntime() default 10;

  /**
   * Specifies the EPSILON parameter for HNSW algorithm.
   * Relative factor that sets the boundaries in which a range query may search for candidates.
   * Vector candidates whose distance from the query vector is radius*(1 + EPSILON)
   * are potentially scanned, allowing more extensive search and more accurate results
   * at the expense of runtime.
   * 
   * @return the EPSILON parameter (defaults to 0.01)
   */
  double epsilon() default 0.01;

  /**
   * Specifies the depth of metamodel creation for nested self-referencing classes.
   * This controls how many levels deep the metamodel generator will traverse
   * when encountering self-referencing nested objects.
   * 
   * @return the depth of metamodel creation for nested self-class
   */
  int depth() default 1;

  /**
   * Indicates whether missing (null) values should be indexed.
   * When enabled, allows searching for documents where this field is missing.
   * 
   * @return {@code true} if missing values should be indexed, {@code false} otherwise
   */
  boolean indexMissing() default false;

  /**
   * Indicates whether empty values should be indexed.
   * When enabled, allows searching for documents where this field is empty.
   * 
   * @return {@code true} if empty values should be indexed, {@code false} otherwise
   */
  boolean indexEmpty() default false;

  /**
   * Indicates whether this field should maintain a sorted set index for
   * lexicographic range queries (greater than, less than, between).
   * When enabled, creates and maintains a Redis sorted set alongside the
   * regular RediSearch index, enabling efficient string range queries.
   *
   * <p>Note: This feature requires additional storage and maintenance overhead
   * as it creates a secondary index structure. Use only when lexicographic
   * range queries are needed.</p>
   *
   * <p>The sorted set key pattern will be: {entityPrefix}{fieldName}:lex
   * where entityPrefix follows the same pattern as the main entity keys.</p>
   *
   * @return {@code true} if lexicographic indexing should be enabled, {@code false} otherwise
   * @since 1.0.0
   */
  boolean lexicographic() default false;

}
