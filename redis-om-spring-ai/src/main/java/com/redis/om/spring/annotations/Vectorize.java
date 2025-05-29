package com.redis.om.spring.annotations;

import java.lang.annotation.*;

import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingModel;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.openai.api.OpenAiApi.EmbeddingModel;

/**
 * Annotation for automatic vectorization of fields in Redis OM Spring entities.
 * <p>
 * This annotation enables automatic generation of vector embeddings from source fields
 * (text, images, etc.) and stores them in designated vector fields for similarity search.
 * The vectorization process occurs automatically during entity persistence.
 * </p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * @Document
 * public class Product {
 * 
 * @Id
 *     private String id;
 * 
 * @Vectorize(destination = "descriptionEmbedding")
 *                        private String description;
 * 
 * @VectorIndexed(algorithm = VectorAlgorithm.HNSW)
 *                          private float[] descriptionEmbedding;
 *                          }
 *                          }</pre>
 * 
 *                          <p>
 *                          The above example will automatically generate embeddings from the {@code description}
 *                          field and store them in the {@code descriptionEmbedding} field whenever the entity
 *                          is saved.
 *                          </p>
 * 
 * @see EmbeddingProvider
 * @see EmbeddingType
 * @see com.redis.om.spring.annotations.VectorIndexed
 * @see com.redis.om.spring.vectorize.Embedder
 * @since 1.0.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface Vectorize {
  /**
   * The name of the field where the generated embedding vector will be stored.
   * <p>
   * This field must exist in the entity class and should be of type {@code float[]},
   * {@code double[]}, or {@code List<Float>}. The field should typically be annotated
   * with {@code @VectorIndexed} to enable vector similarity search.
   * </p>
   * 
   * @return the name of the destination field for the embedding vector
   */
  String destination();

  /**
   * The type of embedding to generate from the source field.
   * <p>
   * Defaults to {@code SENTENCE} for text content, which is suitable for most
   * text-based use cases. Choose the appropriate type based on your data:
   * <ul>
   * <li>{@code SENTENCE} - for text passages, descriptions, reviews</li>
   * <li>{@code WORD} - for individual words or short phrases</li>
   * <li>{@code IMAGE} - for image data</li>
   * <li>{@code FACE} - for facial recognition use cases</li>
   * </ul>
   * 
   * @return the embedding type to use
   */
  EmbeddingType embeddingType() default EmbeddingType.SENTENCE;

  /**
   * The embedding provider to use for vectorization.
   * <p>
   * Defaults to {@code TRANSFORMERS} for local embedding generation. Choose a provider
   * based on your requirements for accuracy, speed, cost, and deployment constraints.
   * </p>
   * 
   * @return the embedding provider to use
   */
  EmbeddingProvider provider() default EmbeddingProvider.TRANSFORMERS;

  /**
   * The Transformers model identifier to use when provider is {@code TRANSFORMERS}.
   * <p>
   * If not specified, a default model appropriate for the embedding type will be used.
   * Examples: "sentence-transformers/all-MiniLM-L6-v2", "bert-base-uncased".
   * </p>
   * 
   * @return the Transformers model identifier
   */
  String transformersModel() default "";

  /**
   * The Transformers tokenizer identifier to use when provider is {@code TRANSFORMERS}.
   * <p>
   * If not specified, the tokenizer associated with the model will be used automatically.
   * </p>
   * 
   * @return the Transformers tokenizer identifier
   */
  String transformersTokenizer() default "";

  /**
   * Configuration for caching Transformers resources.
   * <p>
   * Specifies cache settings for model and tokenizer resources to improve performance
   * when using the Transformers provider.
   * </p>
   * 
   * @return the resource cache configuration string
   */
  String transformersResourceCacheConfiguration() default "";

  /**
   * Additional tokenizer options for the Transformers provider.
   * <p>
   * Allows passing custom options to the tokenizer for fine-tuning text processing.
   * </p>
   * 
   * @return array of tokenizer options
   */
  String[] transformersTokenizerOptions() default {};

  /**
   * The OpenAI embedding model to use when provider is {@code OPENAI}.
   * <p>
   * Defaults to {@code TEXT_EMBEDDING_ADA_002}, which provides good quality embeddings
   * at a reasonable cost. Other models may offer different trade-offs between quality,
   * dimension size, and cost.
   * </p>
   * 
   * @return the OpenAI embedding model
   */
  EmbeddingModel openAiEmbeddingModel() default EmbeddingModel.TEXT_EMBEDDING_ADA_002;

  /**
   * The Ollama model to use when provider is {@code OLLAMA}.
   * <p>
   * Defaults to {@code MISTRAL}. Ollama allows running open-source models locally,
   * providing privacy and control over the embedding generation process.
   * </p>
   * 
   * @return the Ollama model
   */
  OllamaModel ollamaEmbeddingModel() default OllamaModel.MISTRAL;

  /**
   * The Azure OpenAI deployment name to use when provider is {@code AZURE_OPENAI}.
   * <p>
   * This should match the deployment name configured in your Azure OpenAI resource.
   * Defaults to "text-embedding-ada-002".
   * </p>
   * 
   * @return the Azure OpenAI deployment name
   */
  String azureOpenAiDeploymentName() default "text-embedding-ada-002";

  /**
   * The Vertex AI model to use when provider is {@code VERTEX_AI}.
   * <p>
   * Specifies the Google Cloud Vertex AI embedding model. Defaults to "text-embedding-004",
   * which provides high-quality multilingual embeddings.
   * </p>
   * 
   * @return the Vertex AI model identifier
   */
  String vertexAiApiModel() default "text-embedding-004";

  /**
   * The Cohere embedding model to use when provider is {@code AMAZON_BEDROCK_COHERE}.
   * <p>
   * Defaults to {@code COHERE_EMBED_MULTILINGUAL_V3}, which supports over 100 languages
   * and provides high-quality embeddings for multilingual use cases.
   * </p>
   * 
   * @return the Cohere embedding model
   */
  CohereEmbeddingModel cohereEmbeddingModel() default CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V3;

  /**
   * The Amazon Titan embedding model to use when provider is {@code AMAZON_BEDROCK_TITAN}.
   * <p>
   * Defaults to {@code TITAN_EMBED_IMAGE_V1}. Amazon Titan models support both text
   * and image embeddings, providing flexibility for multimodal applications.
   * </p>
   * 
   * @return the Titan embedding model
   */
  TitanEmbeddingModel titanEmbeddingModel() default TitanEmbeddingModel.TITAN_EMBED_IMAGE_V1;
}
