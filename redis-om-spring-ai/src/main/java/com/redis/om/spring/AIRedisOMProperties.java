package com.redis.om.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for Redis OM Spring AI integration.
 * <p>
 * This class provides configuration for various AI and embedding providers including DJL (Deep Java Library),
 * OpenAI, Azure, Vertex AI, AWS Bedrock, and Ollama. These settings control how embeddings are generated
 * for vector similarity search and other AI-powered features in Redis OM Spring.
 * </p>
 * <p>
 * Properties are bound from the {@code redis.om.spring.ai} prefix in application configuration files.
 * The AI features must be explicitly enabled by setting {@code redis.om.spring.ai.enabled=true}.
 * </p>
 *
 * @see com.redis.om.spring.annotations.Vectorize
 * @see com.redis.om.spring.vectorize.Embedder
 */
@ConfigurationProperties(
    prefix = "redis.om.spring.ai", ignoreInvalidFields = true
)
@ConditionalOnProperty(
    name = "redis.om.spring.ai.enabled", havingValue = "true", matchIfMissing = false
)
public class AIRedisOMProperties {

  /**
   * Default constructor for configuration properties binding.
   * This constructor is used by Spring Boot's configuration properties mechanism
   * to create and populate instances of this class from application properties.
   */
  public AIRedisOMProperties() {
    // Default constructor
  }

  /**
   * Flag to enable or disable AI/embedding features in Redis OM Spring.
   * Default is false.
   */
  private boolean enabled = false;

  /**
   * Batch size for processing embeddings when indexing multiple documents.
   * This controls how many documents are processed in a single batch to optimize performance.
   * Default is 1000.
   */
  private int embeddingBatchSize = 1000;

  /**
   * Configuration for Deep Java Library (DJL) embedding providers.
   * Supports image embeddings, sentence embeddings, and face detection/embeddings.
   */
  private final Djl djl = new Djl();

  /**
   * Configuration for OpenAI embedding provider.
   */
  private final OpenAi openAi = new OpenAi();

  /**
   * Configuration for Azure AI services including Azure OpenAI and Azure Entra ID.
   */
  private final AzureClients azure = new AzureClients();

  /**
   * Configuration for Google Vertex AI embedding provider.
   */
  private final VertexAi vertexAi = new VertexAi();

  /**
   * Configuration for AWS embedding providers including Bedrock Cohere and Titan.
   */
  private final Aws aws = new Aws();

  /**
   * Configuration for Ollama local embedding provider.
   */
  private final Ollama ollama = new Ollama();

  /**
   * Returns whether AI/embedding features are enabled.
   *
   * @return true if AI features are enabled, false otherwise
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * Sets whether AI/embedding features are enabled.
   *
   * @param enabled true to enable AI features, false to disable
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Gets the DJL (Deep Java Library) configuration.
   *
   * @return the DJL configuration object
   */
  public Djl getDjl() {
    return djl;
  }

  /**
   * Gets the OpenAI configuration.
   *
   * @return the OpenAI configuration object
   */
  public OpenAi getOpenAi() {
    return openAi;
  }

  /**
   * Gets the Azure AI services configuration.
   *
   * @return the Azure configuration object containing OpenAI and Entra ID settings
   */
  public AzureClients getAzure() {
    return azure;
  }

  /**
   * Gets the Google Vertex AI configuration.
   *
   * @return the Vertex AI configuration object
   */
  public VertexAi getVertexAi() {
    return vertexAi;
  }

  /**
   * Gets the AWS configuration for Bedrock services.
   *
   * @return the AWS configuration object
   */
  public Aws getAws() {
    return aws;
  }

  /**
   * Gets the Ollama configuration for local embedding models.
   *
   * @return the Ollama configuration object
   */
  public Ollama getOllama() {
    return ollama;
  }

  /**
   * Gets the batch size for embedding processing.
   *
   * @return the number of documents to process in a single batch
   */
  public int getEmbeddingBatchSize() {
    return embeddingBatchSize;
  }

  /**
   * Sets the batch size for embedding processing.
   *
   * @param embeddingBatchSize the number of documents to process in a single batch
   */
  public void setEmbeddingBatchSize(int embeddingBatchSize) {
    this.embeddingBatchSize = embeddingBatchSize;
  }

  /**
   * Configuration for Deep Java Library (DJL) embedding providers.
   * <p>
   * DJL provides access to various deep learning models for generating embeddings from images,
   * text, and faces. This configuration class allows customization of model engines, URLs,
   * and preprocessing parameters.
   * </p>
   */
  public static class Djl {
    private static final String DEFAULT_ENGINE = "PyTorch";

    // image embedding settings
    /**
     * The deep learning engine for image embeddings (e.g., PyTorch, TensorFlow).
     * Default is "PyTorch".
     */
    @NotNull
    private String imageEmbeddingModelEngine = DEFAULT_ENGINE;

    /**
     * URL or identifier for the image embedding model.
     * Default is "djl://ai.djl.pytorch/resnet18_embedding".
     */
    @NotNull
    private String imageEmbeddingModelModelUrls = "djl://ai.djl.pytorch/resnet18_embedding";

    /**
     * Width to resize images before embedding generation.
     * Default is 224 pixels.
     */
    private int defaultImagePipelineResizeWidth = 224;

    /**
     * Height to resize images before embedding generation.
     * Default is 224 pixels.
     */
    private int defaultImagePipelineResizeHeight = 224;

    /**
     * Whether to apply center cropping to images during preprocessing.
     * Default is true.
     */
    private boolean defaultImagePipelineCenterCrop = true;

    // sentence tokenizer settings
    /**
     * Maximum length for sentence tokenization.
     * Default is "768".
     */
    @NotNull
    private String sentenceTokenizerMaxLength = "768";

    /**
     * Maximum model length for sentence tokenization.
     * Default is "768".
     */
    @NotNull
    private String sentenceTokenizerModelMaxLength = "768";

    /**
     * Model identifier for sentence embeddings.
     * Default is "https://huggingface.co/sentence-transformers/msmarco-distilbert-dot-v5".
     */
    @NotNull
    private String sentenceTokenizerModel = "https://huggingface.co/sentence-transformers/msmarco-distilbert-dot-v5";

    // face detection
    /**
     * The deep learning engine for face detection.
     * Default is "PyTorch".
     */
    @NotNull
    private String faceDetectionModelEngine = DEFAULT_ENGINE;

    /**
     * Name of the face detection model.
     * Default is "retinaface".
     */
    @NotNull
    private String faceDetectionModelName = "retinaface";

    /**
     * URL for downloading the face detection model.
     * Default is "https://resources.djl.ai/test-models/pytorch/retinaface.zip".
     */
    @NotNull
    private String faceDetectionModelModelUrls = "https://resources.djl.ai/test-models/pytorch/retinaface.zip";

    // face embeddings
    /**
     * The deep learning engine for face embeddings.
     * Default is "PyTorch".
     */
    @NotNull
    private String faceEmbeddingModelEngine = DEFAULT_ENGINE;

    /**
     * Name of the face embedding model.
     * Default is "face_feature".
     */
    @NotNull
    private String faceEmbeddingModelName = "face_feature";

    /**
     * URL for downloading the face embedding model.
     * Default is "https://resources.djl.ai/test-models/pytorch/face_feature.zip".
     */
    @NotNull
    private String faceEmbeddingModelModelUrls = "https://resources.djl.ai/test-models/pytorch/face_feature.zip";

    /**
     * Default constructor.
     */
    public Djl() {
    }

    /**
     * Gets the deep learning engine for image embeddings.
     *
     * @return the engine name (e.g., "PyTorch", "TensorFlow")
     */
    public @NotNull String getImageEmbeddingModelEngine() {
      return this.imageEmbeddingModelEngine;
    }

    /**
     * Sets the deep learning engine for image embeddings.
     *
     * @param imageEmbeddingModelEngine the engine name (e.g., "PyTorch", "TensorFlow")
     */
    public void setImageEmbeddingModelEngine(@NotNull String imageEmbeddingModelEngine) {
      this.imageEmbeddingModelEngine = imageEmbeddingModelEngine;
    }

    /**
     * Gets the URL or identifier for the image embedding model.
     *
     * @return the model URL or DJL model identifier
     */
    public @NotNull String getImageEmbeddingModelModelUrls() {
      return this.imageEmbeddingModelModelUrls;
    }

    /**
     * Sets the URL or identifier for the image embedding model.
     *
     * @param imageEmbeddingModelModelUrls the model URL or DJL model identifier
     */
    public void setImageEmbeddingModelModelUrls(@NotNull String imageEmbeddingModelModelUrls) {
      this.imageEmbeddingModelModelUrls = imageEmbeddingModelModelUrls;
    }

    /**
     * Gets the width to resize images before embedding generation.
     *
     * @return the resize width in pixels
     */
    public int getDefaultImagePipelineResizeWidth() {
      return this.defaultImagePipelineResizeWidth;
    }

    /**
     * Sets the width to resize images before embedding generation.
     *
     * @param defaultImagePipelineResizeWidth the resize width in pixels
     */
    public void setDefaultImagePipelineResizeWidth(int defaultImagePipelineResizeWidth) {
      this.defaultImagePipelineResizeWidth = defaultImagePipelineResizeWidth;
    }

    /**
     * Gets the height to resize images before embedding generation.
     *
     * @return the resize height in pixels
     */
    public int getDefaultImagePipelineResizeHeight() {
      return this.defaultImagePipelineResizeHeight;
    }

    /**
     * Sets the height to resize images before embedding generation.
     *
     * @param defaultImagePipelineResizeHeight the resize height in pixels
     */
    public void setDefaultImagePipelineResizeHeight(int defaultImagePipelineResizeHeight) {
      this.defaultImagePipelineResizeHeight = defaultImagePipelineResizeHeight;
    }

    /**
     * Checks if center cropping is enabled for image preprocessing.
     *
     * @return true if center cropping is enabled, false otherwise
     */
    public boolean isDefaultImagePipelineCenterCrop() {
      return this.defaultImagePipelineCenterCrop;
    }

    /**
     * Sets whether to apply center cropping to images during preprocessing.
     *
     * @param defaultImagePipelineCenterCrop true to enable center cropping, false otherwise
     */
    public void setDefaultImagePipelineCenterCrop(boolean defaultImagePipelineCenterCrop) {
      this.defaultImagePipelineCenterCrop = defaultImagePipelineCenterCrop;
    }

    /**
     * Gets the maximum length for sentence tokenization.
     *
     * @return the maximum token length as a string
     */
    public @NotNull String getSentenceTokenizerMaxLength() {
      return this.sentenceTokenizerMaxLength;
    }

    /**
     * Sets the maximum length for sentence tokenization.
     *
     * @param sentenceTokenizerMaxLength the maximum token length as a string
     */
    public void setSentenceTokenizerMaxLength(@NotNull String sentenceTokenizerMaxLength) {
      this.sentenceTokenizerMaxLength = sentenceTokenizerMaxLength;
    }

    /**
     * Gets the maximum model length for sentence tokenization.
     *
     * @return the maximum model length as a string
     */
    public @NotNull String getSentenceTokenizerModelMaxLength() {
      return this.sentenceTokenizerModelMaxLength;
    }

    /**
     * Sets the maximum model length for sentence tokenization.
     *
     * @param sentenceTokenizerModelMaxLength the maximum model length as a string
     */
    public void setSentenceTokenizerModelMaxLength(@NotNull String sentenceTokenizerModelMaxLength) {
      this.sentenceTokenizerModelMaxLength = sentenceTokenizerModelMaxLength;
    }

    /**
     * Gets the model identifier for sentence embeddings.
     *
     * @return the sentence transformer model name
     */
    public @NotNull String getSentenceTokenizerModel() {
      return this.sentenceTokenizerModel;
    }

    /**
     * Sets the model identifier for sentence embeddings.
     *
     * @param sentenceTokenizerModel the sentence transformer model name
     */
    public void setSentenceTokenizerModel(@NotNull String sentenceTokenizerModel) {
      this.sentenceTokenizerModel = sentenceTokenizerModel;
    }

    /**
     * Gets the deep learning engine for face detection.
     *
     * @return the engine name (e.g., "PyTorch", "TensorFlow")
     */
    public @NotNull String getFaceDetectionModelEngine() {
      return this.faceDetectionModelEngine;
    }

    /**
     * Sets the deep learning engine for face detection.
     *
     * @param faceDetectionModelEngine the engine name (e.g., "PyTorch", "TensorFlow")
     */
    public void setFaceDetectionModelEngine(@NotNull String faceDetectionModelEngine) {
      this.faceDetectionModelEngine = faceDetectionModelEngine;
    }

    /**
     * Gets the name of the face detection model.
     *
     * @return the model name
     */
    public @NotNull String getFaceDetectionModelName() {
      return this.faceDetectionModelName;
    }

    /**
     * Sets the name of the face detection model.
     *
     * @param faceDetectionModelName the model name
     */
    public void setFaceDetectionModelName(@NotNull String faceDetectionModelName) {
      this.faceDetectionModelName = faceDetectionModelName;
    }

    /**
     * Gets the URL for downloading the face detection model.
     *
     * @return the model download URL
     */
    public @NotNull String getFaceDetectionModelModelUrls() {
      return this.faceDetectionModelModelUrls;
    }

    /**
     * Sets the URL for downloading the face detection model.
     *
     * @param faceDetectionModelModelUrls the model download URL
     */
    public void setFaceDetectionModelModelUrls(@NotNull String faceDetectionModelModelUrls) {
      this.faceDetectionModelModelUrls = faceDetectionModelModelUrls;
    }

    /**
     * Gets the deep learning engine for face embeddings.
     *
     * @return the engine name (e.g., "PyTorch", "TensorFlow")
     */
    public @NotNull String getFaceEmbeddingModelEngine() {
      return this.faceEmbeddingModelEngine;
    }

    /**
     * Sets the deep learning engine for face embeddings.
     *
     * @param faceEmbeddingModelEngine the engine name (e.g., "PyTorch", "TensorFlow")
     */
    public void setFaceEmbeddingModelEngine(@NotNull String faceEmbeddingModelEngine) {
      this.faceEmbeddingModelEngine = faceEmbeddingModelEngine;
    }

    /**
     * Gets the name of the face embedding model.
     *
     * @return the model name
     */
    public @NotNull String getFaceEmbeddingModelName() {
      return this.faceEmbeddingModelName;
    }

    /**
     * Sets the name of the face embedding model.
     *
     * @param faceEmbeddingModelName the model name
     */
    public void setFaceEmbeddingModelName(@NotNull String faceEmbeddingModelName) {
      this.faceEmbeddingModelName = faceEmbeddingModelName;
    }

    /**
     * Gets the URL for downloading the face embedding model.
     *
     * @return the model download URL
     */
    public @NotNull String getFaceEmbeddingModelModelUrls() {
      return this.faceEmbeddingModelModelUrls;
    }

    /**
     * Sets the URL for downloading the face embedding model.
     *
     * @param faceEmbeddingModelModelUrls the model download URL
     */
    public void setFaceEmbeddingModelModelUrls(@NotNull String faceEmbeddingModelModelUrls) {
      this.faceEmbeddingModelModelUrls = faceEmbeddingModelModelUrls;
    }

    /**
     * Returns a string representation of the DJL configuration.
     *
     * @return a string containing all DJL configuration values
     */
    public String toString() {
      return "RedisOMSpringProperties.Ai.Djl(imageEmbeddingModelEngine=" + this
          .getImageEmbeddingModelEngine() + ", imageEmbeddingModelModelUrls=" + this
              .getImageEmbeddingModelModelUrls() + ", defaultImagePipelineResizeWidth=" + this
                  .getDefaultImagePipelineResizeWidth() + ", defaultImagePipelineResizeHeight=" + this
                      .getDefaultImagePipelineResizeHeight() + ", defaultImagePipelineCenterCrop=" + this
                          .isDefaultImagePipelineCenterCrop() + ", sentenceTokenizerMaxLength=" + this
                              .getSentenceTokenizerMaxLength() + ", sentenceTokenizerModelMaxLength=" + this
                                  .getSentenceTokenizerModelMaxLength() + ", sentenceTokenizerModel=" + this
                                      .getSentenceTokenizerModel() + ", faceDetectionModelEngine=" + this
                                          .getFaceDetectionModelEngine() + ", faceDetectionModelName=" + this
                                              .getFaceDetectionModelName() + ", faceDetectionModelModelUrls=" + this
                                                  .getFaceDetectionModelModelUrls() + ", faceEmbeddingModelEngine=" + this
                                                      .getFaceEmbeddingModelEngine() + ", faceEmbeddingModelName=" + this
                                                          .getFaceEmbeddingModelName() + ", faceEmbeddingModelModelUrls=" + this
                                                              .getFaceEmbeddingModelModelUrls() + ")";
    }
  }

  /**
   * Configuration for OpenAI embedding provider.
   * <p>
   * Provides settings for connecting to OpenAI's API for generating text embeddings.
   * </p>
   */
  public static class OpenAi {
    /**
     * Default constructor for configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public OpenAi() {
      // Default constructor for configuration properties binding
    }

    /**
     * OpenAI API key for authentication.
     */
    private String apiKey;

    /**
     * Response timeout in seconds for OpenAI API calls.
     * Default is 60 seconds.
     */
    private long responseTimeOut = 60;

    /**
     * Gets the OpenAI API key.
     *
     * @return the API key
     */
    public String getApiKey() {
      return apiKey;
    }

    /**
     * Sets the OpenAI API key.
     *
     * @param apiKey the API key for authentication
     */
    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    /**
     * Gets the response timeout for OpenAI API calls.
     *
     * @return the timeout in seconds
     */
    public long getResponseTimeOut() {
      return responseTimeOut;
    }

    /**
     * Sets the response timeout for OpenAI API calls.
     *
     * @param responseTimeOut the timeout in seconds
     */
    public void setResponseTimeOut(long responseTimeOut) {
      this.responseTimeOut = responseTimeOut;
    }
  }

  /**
   * Configuration for Ollama local embedding provider.
   * <p>
   * Ollama allows running large language models locally for generating embeddings
   * without requiring external API calls.
   * </p>
   */
  public static class Ollama {
    /**
     * Default constructor for configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public Ollama() {
      // Default constructor for configuration properties binding
    }

    /**
     * Base URL for the Ollama service.
     * Default is "http://localhost:11434".
     */
    private String baseUrl = "http://localhost:11434";

    /**
     * Gets the base URL for the Ollama service.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
      return baseUrl;
    }

    /**
     * Sets the base URL for the Ollama service.
     *
     * @param baseUrl the base URL (e.g., "http://localhost:11434")
     */
    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }
  }

  /**
   * Configuration for Azure AI services.
   * <p>
   * Contains configuration for both Azure OpenAI and Azure Entra ID (formerly Azure Active Directory)
   * authentication methods.
   * </p>
   */
  public static class AzureClients {
    /**
     * Default constructor for configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public AzureClients() {
      // Default constructor for configuration properties binding
    }

    /**
     * Configuration for Azure OpenAI service.
     */
    private AzureOpenAi openAi;

    /**
     * Configuration for Azure Entra ID authentication.
     */
    private AzureEntraId entraId;

    /**
     * Gets the Azure OpenAI configuration.
     *
     * @return the Azure OpenAI configuration object
     */
    public AzureOpenAi getOpenAi() {
      return openAi;
    }

    /**
     * Sets the Azure OpenAI configuration.
     *
     * @param openAi the Azure OpenAI configuration object
     */
    public void setOpenAi(AzureOpenAi openAi) {
      this.openAi = openAi;
    }

    /**
     * Gets the Azure Entra ID configuration.
     *
     * @return the Azure Entra ID configuration object
     */
    public AzureEntraId getEntraId() {
      return entraId;
    }

    /**
     * Sets the Azure Entra ID configuration.
     *
     * @param entraId the Azure Entra ID configuration object
     */
    public void setEntraId(AzureEntraId entraId) {
      this.entraId = entraId;
    }
  }

  /**
   * Configuration for Azure OpenAI service.
   * <p>
   * Provides settings for connecting to Azure OpenAI for generating embeddings.
   * </p>
   */
  public static class AzureOpenAi {
    /**
     * Default constructor for configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public AzureOpenAi() {
      // Default constructor for configuration properties binding
    }

    /**
     * API key for Azure OpenAI authentication.
     */
    private String apiKey;

    /**
     * Azure OpenAI endpoint URL.
     */
    private String endpoint;

    /**
     * Gets the Azure OpenAI API key.
     *
     * @return the API key
     */
    public String getApiKey() {
      return apiKey;
    }

    /**
     * Sets the Azure OpenAI API key.
     *
     * @param apiKey the API key for authentication
     */
    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    /**
     * Gets the Azure OpenAI endpoint URL.
     *
     * @return the endpoint URL
     */
    public String getEndpoint() {
      return endpoint;
    }

    /**
     * Sets the Azure OpenAI endpoint URL.
     *
     * @param endpoint the endpoint URL
     */
    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }
  }

  /**
   * Configuration for Azure Entra ID (formerly Azure Active Directory) authentication.
   * <p>
   * Provides OAuth 2.0 client credentials flow authentication for Azure services.
   * </p>
   */
  public static class AzureEntraId {
    /**
     * Default constructor for configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public AzureEntraId() {
      // Default constructor for configuration properties binding
    }

    /**
     * Flag to enable Azure Entra ID authentication.
     * Default is false.
     */
    private boolean enabled = false;

    /**
     * API key for fallback authentication.
     */
    private String apiKey;

    /**
     * Azure service endpoint URL.
     */
    private String endpoint;

    /**
     * Azure tenant ID for authentication.
     */
    private String tenantId;

    /**
     * Client ID (application ID) for OAuth authentication.
     */
    private String clientId;

    /**
     * Client secret for OAuth authentication.
     */
    private String clientSecret;

    /**
     * Gets the API key for fallback authentication.
     *
     * @return the API key
     */
    public String getApiKey() {
      return apiKey;
    }

    /**
     * Sets the API key for fallback authentication.
     *
     * @param apiKey the API key
     */
    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    /**
     * Gets the Azure service endpoint URL.
     *
     * @return the endpoint URL
     */
    public String getEndpoint() {
      return endpoint;
    }

    /**
     * Sets the Azure service endpoint URL.
     *
     * @param endpoint the endpoint URL
     */
    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    /**
     * Checks if Azure Entra ID authentication is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether Azure Entra ID authentication is enabled.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Gets the Azure tenant ID.
     *
     * @return the tenant ID
     */
    public String getTenantId() {
      return tenantId;
    }

    /**
     * Sets the Azure tenant ID.
     *
     * @param tenantId the tenant ID
     */
    public void setTenantId(String tenantId) {
      this.tenantId = tenantId;
    }

    /**
     * Gets the client ID (application ID).
     *
     * @return the client ID
     */
    public String getClientId() {
      return clientId;
    }

    /**
     * Sets the client ID (application ID).
     *
     * @param clientId the client ID
     */
    public void setClientId(String clientId) {
      this.clientId = clientId;
    }

    /**
     * Gets the client secret.
     *
     * @return the client secret
     */
    public String getClientSecret() {
      return clientSecret;
    }

    /**
     * Sets the client secret.
     *
     * @param clientSecret the client secret
     */
    public void setClientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
    }
  }

  /**
   * Configuration for Google Vertex AI embedding provider.
   * <p>
   * Provides settings for connecting to Google Cloud's Vertex AI service for generating embeddings.
   * </p>
   */
  public static class VertexAi {
    /**
     * Default constructor for configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public VertexAi() {
      // Default constructor for configuration properties binding
    }

    /**
     * API key for Vertex AI authentication (if using API key auth).
     */
    private String apiKey;

    /**
     * Vertex AI endpoint URL.
     */
    private String endpoint;

    /**
     * Google Cloud project ID.
     */
    private String projectId;

    /**
     * Google Cloud location/region for the Vertex AI service.
     */
    private String location;

    /**
     * Gets the Google Cloud project ID.
     *
     * @return the project ID
     */
    public String getProjectId() {
      return projectId;
    }

    /**
     * Sets the Google Cloud project ID.
     *
     * @param projectId the project ID
     */
    public void setProjectId(String projectId) {
      this.projectId = projectId;
    }

    /**
     * Gets the Google Cloud location/region.
     *
     * @return the location (e.g., "us-central1")
     */
    public String getLocation() {
      return location;
    }

    /**
     * Sets the Google Cloud location/region.
     *
     * @param location the location (e.g., "us-central1")
     */
    public void setLocation(String location) {
      this.location = location;
    }

    /**
     * Gets the Vertex AI API key.
     *
     * @return the API key
     */
    public String getApiKey() {
      return apiKey;
    }

    /**
     * Sets the Vertex AI API key.
     *
     * @param apiKey the API key for authentication
     */
    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    /**
     * Gets the Vertex AI endpoint URL.
     *
     * @return the endpoint URL
     */
    public String getEndpoint() {
      return endpoint;
    }

    /**
     * Sets the Vertex AI endpoint URL.
     *
     * @param endpoint the endpoint URL
     */
    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }
  }

  /**
   * Configuration for AWS embedding providers.
   * <p>
   * Provides settings for AWS Bedrock services including Cohere and Titan models.
   * </p>
   */
  public static class Aws {
    /**
     * AWS region for Bedrock services.
     */
    private String region;

    /**
     * AWS access key for authentication.
     */
    private String accessKey;

    /**
     * AWS secret key for authentication.
     */
    private String secretKey;

    /**
     * Configuration for Bedrock Cohere model.
     */
    private BedrockCohere bedrockCohere = new BedrockCohere();

    /**
     * Configuration for Bedrock Titan model.
     */
    private BedrockTitan bedrockTitan = new BedrockTitan();

    /**
     * Default constructor for configuration properties binding.
     */
    public Aws() {
      // Default constructor for Spring Boot configuration properties
    }

    /**
     * Gets the AWS region.
     *
     * @return the region (e.g., "us-east-1")
     */
    public String getRegion() {
      return region;
    }

    /**
     * Sets the AWS region.
     *
     * @param region the region (e.g., "us-east-1")
     */
    public void setRegion(String region) {
      this.region = region;
    }

    /**
     * Gets the AWS access key.
     *
     * @return the access key
     */
    public String getAccessKey() {
      return accessKey;
    }

    /**
     * Sets the AWS access key.
     *
     * @param accessKey the access key for authentication
     */
    public void setAccessKey(String accessKey) {
      this.accessKey = accessKey;
    }

    /**
     * Gets the AWS secret key.
     *
     * @return the secret key
     */
    public String getSecretKey() {
      return secretKey;
    }

    /**
     * Sets the AWS secret key.
     *
     * @param secretKey the secret key for authentication
     */
    public void setSecretKey(String secretKey) {
      this.secretKey = secretKey;
    }

    /**
     * Gets the Bedrock Cohere configuration.
     *
     * @return the Bedrock Cohere configuration object
     */
    public BedrockCohere getBedrockCohere() {
      return bedrockCohere;
    }

    /**
     * Sets the Bedrock Cohere configuration.
     *
     * @param bedrockCohere the Bedrock Cohere configuration object
     */
    public void setBedrockCohere(BedrockCohere bedrockCohere) {
      this.bedrockCohere = bedrockCohere;
    }

    /**
     * Gets the Bedrock Titan configuration.
     *
     * @return the Bedrock Titan configuration object
     */
    public BedrockTitan getBedrockTitan() {
      return bedrockTitan;
    }

    /**
     * Sets the Bedrock Titan configuration.
     *
     * @param bedrockTitan the Bedrock Titan configuration object
     */
    public void setBedrockTitan(BedrockTitan bedrockTitan) {
      this.bedrockTitan = bedrockTitan;
    }

    /**
     * Configuration for AWS Bedrock Cohere model.
     */
    public static class BedrockCohere {
      /**
       * Default constructor for configuration properties binding.
       * This constructor is used by Spring Boot's configuration properties mechanism
       * to create and populate instances of this class from application properties.
       */
      public BedrockCohere() {
        // Default constructor for configuration properties binding
      }

      /**
       * Response timeout in seconds for Bedrock Cohere API calls.
       * Default is 60 seconds.
       */
      private int responseTimeOut = 60;

      /**
       * Gets the response timeout for Bedrock Cohere API calls.
       *
       * @return the timeout in seconds
       */
      public int getResponseTimeOut() {
        return responseTimeOut;
      }

      /**
       * Sets the response timeout for Bedrock Cohere API calls.
       *
       * @param responseTimeOut the timeout in seconds
       */
      public void setResponseTimeOut(int responseTimeOut) {
        this.responseTimeOut = responseTimeOut;
      }
    }

    /**
     * Configuration for AWS Bedrock Titan model.
     */
    public static class BedrockTitan {
      /**
       * Default constructor for configuration properties binding.
       * This constructor is used by Spring Boot's configuration properties mechanism
       * to create and populate instances of this class from application properties.
       */
      public BedrockTitan() {
        // Default constructor for configuration properties binding
      }

      /**
       * Response timeout in seconds for Bedrock Titan API calls.
       * Default is 300 seconds (5 minutes).
       */
      private long responseTimeOut = 300;

      /**
       * Gets the response timeout for Bedrock Titan API calls.
       *
       * @return the timeout in seconds
       */
      public long getResponseTimeOut() {
        return responseTimeOut;
      }

      /**
       * Sets the response timeout for Bedrock Titan API calls.
       *
       * @param responseTimeOut the timeout in seconds
       */
      public void setResponseTimeOut(long responseTimeOut) {
        this.responseTimeOut = responseTimeOut;
      }
    }
  }
}
