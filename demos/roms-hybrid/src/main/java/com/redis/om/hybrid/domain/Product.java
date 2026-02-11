package com.redis.om.hybrid.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.indexing.DistanceMetric;
import com.redis.om.spring.indexing.VectorType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

/**
 * Product entity for demonstrating hybrid search capabilities.
 *
 * Combines full-text search on product descriptions with vector similarity
 * search on product embeddings to enable semantic product discovery.
 */
@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@NoArgsConstructor
@RedisHash
public class Product {

  @Id
  private String id;

  /**
   * Product name - indexed for tag-based filtering
   */
  @Indexed
  @NonNull
  private String name;

  /**
   * Product category - indexed for tag-based filtering
   */
  @Indexed
  @NonNull
  private String category;

  /**
   * Product description - full-text searchable for BM25 scoring
   * This field is used for the text component of hybrid search
   */
  @Searchable
  @NonNull
  private String description;

  /**
   * Product price - numeric field for range filtering
   */
  @Indexed
  @NonNull
  private Double price;

  /**
   * Product embedding vector - used for semantic similarity search
   * This field is used for the vector component of hybrid search
   *
   * Dimension: 384 (typical for sentence transformers like all-MiniLM-L6-v2)
   */
  @Indexed(
      schemaFieldType = SchemaFieldType.VECTOR, algorithm = VectorAlgorithm.FLAT, type = VectorType.FLOAT32,
      dimension = 384, distanceMetric = DistanceMetric.COSINE
  )
  @NonNull
  private byte[] embedding;

  /**
   * Stock quantity - numeric field
   */
  @Indexed
  @NonNull
  private Integer stock;

  /**
   * Product brand - indexed for filtering
   */
  @Indexed
  @NonNull
  private String brand;
}
