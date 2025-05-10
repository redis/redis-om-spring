package com.redis.om.spring.annotations;

import com.redis.om.spring.indexing.DistanceMetric;
import com.redis.om.spring.indexing.VectorType;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
public @interface Indexed {
  // by default, attempt to determine the schema field type from the Java datatype
  SchemaFieldType schemaFieldType() default SchemaFieldType.AUTODETECT;

  SerializationHint serializationHint() default SerializationHint.NONE;

  String fieldName() default "";

  String alias() default "";

  boolean sortable() default false;

  boolean noindex() default false;

  double weight() default 1.0;

  boolean nostem() default false;

  String phonetic() default "";

  String separator() default "|";

  int arrayIndex() default Integer.MIN_VALUE;

  // -----------------
  // VECTOR properties
  // -----------------

  // Indexing methods
  VectorAlgorithm algorithm() default VectorAlgorithm.FLAT;

  // Vector type. Current supported types are FLOAT32 and FLOAT64.
  VectorType type() default VectorType.FLOAT32;

  // Specifies the number of attributes for the index. Must be specified.
  // Counts the total number of attributes passed for the index in the command,
  // although algorithm parameters should be submitted as named arguments.
  int count() default Integer.MIN_VALUE;

  // Vector dimension specified as a positive integer
  int dimension() default Integer.MIN_VALUE;

  // Supported distance metric, one of {L2, IP, COSINE}.
  DistanceMetric distanceMetric() default DistanceMetric.L2;

  // Initial vector capacity in the index affecting memory allocation size of the index.
  int initialCapacity() default Integer.MIN_VALUE;

  // For FLAT

  // Block size to hold BLOCK_SIZE amount of vectors in a contiguous array. This is useful when the index is dynamic
  // with respect to addition and deletion. Defaults to 1024
  int blockSize() default 1024;

  // For HNSW
  // M - Number of maximum allowed outgoing edges for each node in the graph in each layer.
  // on layer zero the maximal number of outgoing edges will be 2M. Default is 16.
  int m() default 16;

  // EF_CONSTRUCTION - Number of maximum allowed potential outgoing edges candidates for each node
  // in the graph, during the graph building. Default is 200.
  int efConstruction() default 200;

  // EF_RUNTIME - Number of maximum top candidates to hold during the KNN search.
  // Higher values of EF_RUNTIME lead to more accurate results at the expense of a longer runtime.
  // Default is 10.
  int efRuntime() default 10;

  // EPSILON - Relative factor that sets the boundaries in which a range query may search for candidates.
  // That is, vector candidates whose distance from the query vector is radius*(1 + EPSILON)
  // are potentially scanned, allowing more extensive search and more accurate results
  // (on the expense of runtime). Default is 0.01.
  double epsilon() default 0.01;

  /**
   * @return depth of metamodel creation for nested self-class.
   */
  int depth() default 1;

  // Implement official null support - https://github.com/redis/redis-om-spring/issues/527
  boolean indexMissing() default false;
  boolean indexEmpty() default false;

}
