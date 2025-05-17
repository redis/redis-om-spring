package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.EmbeddingType;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import com.redis.om.spring.annotations.Vectorize;
import com.redis.om.spring.indexing.DistanceMetric;
import com.redis.om.spring.indexing.VectorType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@NoArgsConstructor(
    force = true
)
@RedisHash
public class Product {
  @Id
  private String id;

  @Indexed
  @NonNull
  private String name;

  @Indexed(
      //
      schemaFieldType = SchemaFieldType.VECTOR, //
      algorithm = VectorAlgorithm.HNSW, //
      type = VectorType.FLOAT32, //
      dimension = 512, //
      distanceMetric = DistanceMetric.COSINE, //
      initialCapacity = 10
  )
  private byte[] imageEmbedding;

  @Vectorize(
      destination = "imageEmbedding", embeddingType = EmbeddingType.IMAGE
  )
  @NonNull
  private String imagePath;

  @Indexed(
      //
      schemaFieldType = SchemaFieldType.VECTOR, //
      algorithm = VectorAlgorithm.HNSW, //
      type = VectorType.FLOAT32, //
      dimension = 384, //
      distanceMetric = DistanceMetric.COSINE, //
      initialCapacity = 10
  )
  private byte[] sentenceEmbedding;

  @Vectorize(
      destination = "sentenceEmbedding", embeddingType = EmbeddingType.SENTENCE
  )
  @NonNull
  private String description;
}
