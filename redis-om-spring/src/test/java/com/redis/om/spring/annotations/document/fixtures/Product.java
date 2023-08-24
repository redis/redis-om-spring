package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.DistanceMetric;
import com.redis.om.spring.VectorType;
import com.redis.om.spring.annotations.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import redis.clients.jedis.search.Schema.VectorField.VectorAlgo;

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
      algorithm = VectorAlgo.HNSW, //
      type = VectorType.FLOAT32, //
      dimension = 512, //
      distanceMetric = DistanceMetric.L2, //
      initialCapacity = 10
  )
  private float[] imageEmbedding;

  @Vectorize(destination = "imageEmbedding", embeddingType = EmbeddingType.IMAGE)
  @NonNull
  private String imagePath;

  @Indexed(//
      schemaFieldType = SchemaFieldType.VECTOR, //
      algorithm = VectorAlgo.HNSW, //
      type = VectorType.FLOAT32, //
      dimension = 768, //
      distanceMetric = DistanceMetric.L2, //
      initialCapacity = 10
  )
  private float[] sentenceEmbedding;

  @Vectorize(destination = "sentenceEmbedding", embeddingType = EmbeddingType.SENTENCE)
  @NonNull
  private String description;
}
