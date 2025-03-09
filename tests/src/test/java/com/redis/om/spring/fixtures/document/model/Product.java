package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.*;
import com.redis.om.spring.indexing.DistanceMetric;
import com.redis.om.spring.indexing.VectorType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor(force = true)
@Document
public class Product {
  @Id
  private String id;

  @Indexed
  @NonNull
  private String name;

  @Indexed(//
           schemaFieldType = SchemaFieldType.VECTOR, //
           algorithm = VectorAlgorithm.HNSW, //
           type = VectorType.FLOAT32, //
           dimension = 512, //
           distanceMetric = DistanceMetric.COSINE, //
           initialCapacity = 10
  )
  private float[] imageEmbedding;

  @Vectorize(destination = "imageEmbedding", embeddingType = EmbeddingType.IMAGE)
  @NonNull
  private String imagePath;

  @Indexed(//
           schemaFieldType = SchemaFieldType.VECTOR, //
           algorithm = VectorAlgorithm.HNSW, //
           type = VectorType.FLOAT32, //
           dimension = 384, //
           distanceMetric = DistanceMetric.COSINE, //
           initialCapacity = 10
  )
  private float[] sentenceEmbedding;

  @Vectorize(destination = "sentenceEmbedding", embeddingType = EmbeddingType.SENTENCE)
  @NonNull
  private String description;
}
