package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.*;
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
public class HashWithOpenAIEmbedding {
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
      dimension = 1536, //
      distanceMetric = DistanceMetric.COSINE, //
      initialCapacity = 10
  )
  private byte[] textEmbedding;

  @Vectorize(
      destination = "textEmbedding", embeddingType = EmbeddingType.SENTENCE, provider = EmbeddingProvider.OPENAI
  )
  @NonNull
  private String text;
}
