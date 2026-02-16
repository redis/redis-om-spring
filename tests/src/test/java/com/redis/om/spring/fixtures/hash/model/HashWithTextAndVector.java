package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.indexing.DistanceMetric;
import com.redis.om.spring.indexing.VectorType;

import lombok.*;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

/**
 * Test fixture for hybrid search combining text and vector fields.
 * Used to test HybridQuery functionality.
 */
@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RedisHash
public class HashWithTextAndVector {
  @Id
  @EqualsAndHashCode.Include
  @NonNull
  private String id;

  /**
   * Full-text searchable description field for text search component of hybrid query
   */
  @Searchable
  @NonNull
  private String description;

  /**
   * Tag field for filtering
   */
  @Indexed
  @NonNull
  private String category;

  /**
   * Numeric field for filtering
   */
  @Indexed
  @NonNull
  private Integer price;

  /**
   * Vector embedding field for vector similarity search component
   */
  @Indexed(
      schemaFieldType = SchemaFieldType.VECTOR,
      algorithm = VectorAlgorithm.FLAT,
      type = VectorType.FLOAT32,
      dimension = 4,
      distanceMetric = DistanceMetric.COSINE
  )
  @NonNull
  private byte[] embedding;
}
