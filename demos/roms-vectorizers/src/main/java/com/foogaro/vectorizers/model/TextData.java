package com.foogaro.vectorizers.model;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.*;
import com.redis.om.spring.indexing.DistanceMetric;
import com.redis.om.spring.indexing.VectorType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.search.schemafields.VectorField;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@Document
public class TextData {
  @Id
  private String id;
  @TextIndexed
  private String name;
  @Vectorize(
      destination = "textEmbedding", embeddingType = EmbeddingType.SENTENCE, provider = EmbeddingProvider.OPENAI,
      openAiEmbeddingModel = OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_ADA_002
  )
  private String description;
  @VectorIndexed(
      algorithm = VectorField.VectorAlgorithm.HNSW, type = VectorType.FLOAT32, dimension = 1536,
      distanceMetric = DistanceMetric.L2, initialCapacity = 10
  )
  private float[] textEmbedding;
  @Indexed
  private int year;
  @NumericIndexed
  private double score;

}
