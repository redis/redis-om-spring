package com.redis.om.spring.annotations;

import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingModel;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.openai.api.OpenAiApi.EmbeddingModel;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
public @interface Vectorize {
  String destination();

  EmbeddingType embeddingType() default EmbeddingType.SENTENCE;

  EmbeddingProvider provider() default EmbeddingProvider.TRANSFORMERS;

  EmbeddingModel openAiEmbeddingModel() default EmbeddingModel.TEXT_EMBEDDING_ADA_002;

  OllamaModel ollamaEmbeddingModel() default OllamaModel.MISTRAL;

  String azureOpenAiDeploymentName() default "text-embedding-ada-002";

  String vertexAiPaLm2ApiModel() default "text-embedding-004";

  CohereEmbeddingModel cohereEmbeddingModel() default CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V3;

  TitanEmbeddingModel titanEmbeddingModel() default TitanEmbeddingModel.TITAN_EMBED_IMAGE_V1;
}
