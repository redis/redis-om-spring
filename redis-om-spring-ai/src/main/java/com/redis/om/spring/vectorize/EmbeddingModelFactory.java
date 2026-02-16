package com.redis.om.spring.vectorize;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingOptions;
import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingModel;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.EmbeddingModel;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingModel;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingOptions;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.redis.om.spring.AIRedisOMProperties;
import com.redis.om.spring.annotations.Vectorize;

import io.micrometer.observation.ObservationRegistry;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

/**
 * Factory class responsible for creating and caching embedding models from various AI providers.
 * 
 * <p>This factory provides a centralized way to create embedding models with the following features:
 * <ul>
 * <li>Model instance caching to avoid recreating expensive model objects</li>
 * <li>Support for multiple AI providers (OpenAI, Azure OpenAI, Ollama, Vertex AI, AWS Bedrock, Transformers)</li>
 * <li>Configuration fallback mechanism (annotation → properties → Spring AI properties → environment variables)</li>
 * <li>Thread-safe model creation and caching</li>
 * </ul>
 * 
 * <p>The factory uses a cache key strategy to ensure that models with different configurations
 * are cached separately, while identical configurations reuse the same model instance.
 * 
 * @see DefaultEmbedder
 * @see AIRedisOMProperties
 * @see SpringAiProperties
 */
public class EmbeddingModelFactory {
  /** Main configuration properties for Redis OM AI features */
  private final AIRedisOMProperties properties;

  /** Spring AI configuration properties used as fallback */
  private final SpringAiProperties springAiProperties;

  /** Thread-safe cache for storing created model instances */
  private final Map<String, Object> modelCache = new ConcurrentHashMap<>();

  /** REST client builder for synchronous HTTP calls */
  private final RestClient.Builder restClientBuilder;

  /** WebClient builder for reactive HTTP calls */
  private final WebClient.Builder webClientBuilder;

  /** Error handler for HTTP responses */
  private final ResponseErrorHandler responseErrorHandler;

  /** Registry for observability and metrics */
  private final ObservationRegistry observationRegistry;

  /**
   * Constructs a new EmbeddingModelFactory with all required dependencies.
   * 
   * @param properties           Redis OM AI configuration properties
   * @param springAiProperties   Spring AI configuration properties for fallback
   * @param restClientBuilder    Builder for creating REST clients
   * @param webClientBuilder     Builder for creating reactive web clients
   * @param responseErrorHandler Handler for HTTP error responses
   * @param observationRegistry  Registry for metrics and observability
   */
  public EmbeddingModelFactory(AIRedisOMProperties properties, SpringAiProperties springAiProperties,
      RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder,
      ResponseErrorHandler responseErrorHandler, ObservationRegistry observationRegistry) {
    this.properties = properties;
    this.springAiProperties = springAiProperties;
    this.restClientBuilder = restClientBuilder;
    this.webClientBuilder = webClientBuilder;
    this.responseErrorHandler = responseErrorHandler;
    this.observationRegistry = observationRegistry;
  }

  /**
   * Generates a cache key for a model based on its type and parameters
   * 
   * @param modelType The type of the model
   * @param params    Parameters that uniquely identify the model configuration
   * @return A string key for caching
   */
  private String generateCacheKey(String modelType, String... params) {
    StringBuilder keyBuilder = new StringBuilder(modelType);
    for (String param : params) {
      keyBuilder.append(":").append(param);
    }
    return keyBuilder.toString();
  }

  /**
   * Clears the model cache, forcing new models to be created on next request.
   * This can be useful when configuration changes or to free up resources.
   */
  public void clearCache() {
    modelCache.clear();
  }

  /**
   * Removes a specific model from the cache.
   *
   * @param modelType The type of the model (e.g., "openai", "transformers")
   * @param params    Parameters that were used to create the model
   * @return true if a model was removed, false otherwise
   */
  public boolean removeFromCache(String modelType, String... params) {
    String cacheKey = generateCacheKey(modelType, params);
    return modelCache.remove(cacheKey) != null;
  }

  /**
   * Returns the current number of models in the cache.
   *
   * @return The number of cached models
   */
  public int getCacheSize() {
    return modelCache.size();
  }

  /**
   * Creates or retrieves a cached Transformers embedding model.
   * 
   * <p>Transformers models run locally and support custom models from Hugging Face.
   * The method caches models based on their configuration to avoid reloading.
   * 
   * @param vectorize Configuration from the @Vectorize annotation
   * @return Configured and initialized TransformersEmbeddingModel
   * @throws RuntimeException if model initialization fails
   */
  public TransformersEmbeddingModel createTransformersEmbeddingModel(Vectorize vectorize) {
    String cacheKey = generateCacheKey("transformers", vectorize.transformersModel(), vectorize.transformersTokenizer(),
        vectorize.transformersResourceCacheConfiguration(), String.join(",", vectorize.transformersTokenizerOptions()));

    TransformersEmbeddingModel cachedModel = (TransformersEmbeddingModel) modelCache.get(cacheKey);

    if (cachedModel != null) {
      return cachedModel;
    }

    TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();

    if (!vectorize.transformersModel().isEmpty()) {
      embeddingModel.setModelResource(vectorize.transformersModel());
    }

    if (!vectorize.transformersTokenizer().isEmpty()) {
      embeddingModel.setTokenizerResource(vectorize.transformersTokenizer());
    }

    if (!vectorize.transformersResourceCacheConfiguration().isEmpty()) {
      embeddingModel.setResourceCacheDirectory(vectorize.transformersResourceCacheConfiguration());
    }

    if (vectorize.transformersTokenizerOptions().length > 0) {
      Map<String, String> options = Arrays.stream(vectorize.transformersTokenizerOptions()).map(entry -> entry.split(
          "=", 2)).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
      embeddingModel.setTokenizerOptions(options);
    }

    try {
      embeddingModel.afterPropertiesSet();
    } catch (Exception e) {
      throw new RuntimeException("Error initializing TransformersEmbeddingModel", e);
    }

    modelCache.put(cacheKey, embeddingModel);

    return embeddingModel;
  }

  /**
   * Creates or retrieves a cached OpenAI embedding model using an enum value.
   *
   * @param model OpenAI embedding model enum
   * @return Configured EmbeddingModel instance for OpenAI
   */
  public org.springframework.ai.embedding.EmbeddingModel createOpenAiEmbeddingModel(EmbeddingModel model) {
    return createOpenAiEmbeddingModel(model.value);
  }

  /**
   * Creates or retrieves a cached OpenAI embedding model.
   *
   * <p>The API key is resolved in the following order:
   * <ol>
   * <li>Redis OM properties</li>
   * <li>Spring AI properties</li>
   * </ol>
   *
   * <p>On Spring Framework 7+, this method uses a compatibility layer that calls
   * the OpenAI API directly, bypassing the broken {@code OpenAiApi} class from
   * Spring AI 1.0.1 which was compiled against Spring Framework 6.
   *
   * @param model Model identifier (e.g., "text-embedding-ada-002")
   * @return Configured EmbeddingModel instance for OpenAI
   */
  public org.springframework.ai.embedding.EmbeddingModel createOpenAiEmbeddingModel(String model) {
    String cacheKey = generateCacheKey("openai", model, properties.getOpenAi().getApiKey());
    org.springframework.ai.embedding.EmbeddingModel cachedModel = (org.springframework.ai.embedding.EmbeddingModel) modelCache
        .get(cacheKey);

    if (cachedModel != null) {
      return cachedModel;
    }

    String apiKey = properties.getOpenAi().getApiKey();
    if (!StringUtils.hasText(apiKey)) {
      apiKey = springAiProperties.getOpenai().getApiKey();
      properties.getOpenAi().setApiKey(apiKey);
    }

    org.springframework.ai.embedding.EmbeddingModel embeddingModel;

    if (OpenAiCompat.NEEDED) {
      // Spring Framework 7+: use direct REST client to bypass broken OpenAiApi
      embeddingModel = OpenAiCompat.createEmbeddingModel(properties.getOpenAi().getApiKey(), model, properties
          .getOpenAi().getResponseTimeOut());
    } else {
      // Spring Framework 6: use standard Spring AI OpenAiApi path
      SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
      factory.setReadTimeout(Duration.ofSeconds(properties.getOpenAi().getResponseTimeOut()));

      OpenAiApi openAiApi = OpenAiApi.builder().apiKey(properties.getOpenAi().getApiKey()).restClientBuilder(RestClient
          .builder().requestFactory(factory)).build();

      embeddingModel = new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, OpenAiEmbeddingOptions.builder().model(
          model).build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    modelCache.put(cacheKey, embeddingModel);
    return embeddingModel;
  }

  /**
   * Creates an Azure OpenAI client with appropriate authentication.
   * 
   * <p>Supports both API key and Entra ID (Azure AD) authentication methods.
   * 
   * @return Configured OpenAIClient for Azure
   */
  private OpenAIClient getOpenAIClient() {
    OpenAIClientBuilder builder = new OpenAIClientBuilder();
    if (properties.getAzure().getEntraId().isEnabled()) {
      builder.credential(new DefaultAzureCredentialBuilder().tenantId(properties.getAzure().getEntraId().getTenantId())
          .build()).endpoint(properties.getAzure().getEntraId().getEndpoint());
    } else {
      builder.credential(new AzureKeyCredential(properties.getAzure().getOpenAi().getApiKey())).endpoint(properties
          .getAzure().getOpenAi().getEndpoint());
    }
    return builder.buildClient();
  }

  /**
   * Creates or retrieves a cached Azure OpenAI embedding model.
   * 
   * <p>Supports both API key and Entra ID authentication. Configuration is resolved
   * from Redis OM properties with fallback to Spring AI properties.
   * 
   * @param deploymentName Azure OpenAI deployment name
   * @return Configured AzureOpenAiEmbeddingModel instance
   */
  public AzureOpenAiEmbeddingModel createAzureOpenAiEmbeddingModel(String deploymentName) {
    String cacheKey = generateCacheKey("azure-openai", deploymentName, properties.getAzure().getOpenAi().getApiKey(),
        properties.getAzure().getOpenAi().getEndpoint(), String.valueOf(properties.getAzure().getEntraId()
            .isEnabled()));

    AzureOpenAiEmbeddingModel cachedModel = (AzureOpenAiEmbeddingModel) modelCache.get(cacheKey);

    if (cachedModel != null) {
      return cachedModel;
    }

    String apiKey = properties.getAzure().getOpenAi().getApiKey();
    if (!StringUtils.hasText(apiKey)) {
      apiKey = springAiProperties.getAzure().getApiKey(); // Fallback to Spring AI property
      properties.getAzure().getOpenAi().setApiKey(apiKey);
    }

    String endpoint = properties.getAzure().getOpenAi().getEndpoint();
    if (!StringUtils.hasText(endpoint)) {
      endpoint = springAiProperties.getAzure().getEndpoint(); // Fallback to Spring AI property
      properties.getAzure().getOpenAi().setEndpoint(endpoint);
    }

    OpenAIClient openAIClient = getOpenAIClient();

    AzureOpenAiEmbeddingOptions options = AzureOpenAiEmbeddingOptions.builder().deploymentName(deploymentName).build();

    AzureOpenAiEmbeddingModel embeddingModel = new AzureOpenAiEmbeddingModel(openAIClient, MetadataMode.EMBED, options);

    modelCache.put(cacheKey, embeddingModel);

    return embeddingModel;
  }

  /**
   * Creates or retrieves a cached Vertex AI text embedding model.
   * 
   * <p>Configuration is resolved in the following order:
   * <ol>
   * <li>Redis OM properties</li>
   * <li>Spring AI properties</li>
   * <li>Environment variables (VERTEX_AI_API_KEY)</li>
   * <li>System properties (SPRING_AI_VERTEX_AI_API_KEY)</li>
   * </ol>
   * 
   * @param model Model identifier for Vertex AI
   * @return Configured VertexAiTextEmbeddingModel instance
   */
  public VertexAiTextEmbeddingModel createVertexAiTextEmbeddingModel(String model) {
    String cacheKey = generateCacheKey("vertex-ai", model, properties.getVertexAi().getApiKey(), properties
        .getVertexAi().getEndpoint(), properties.getVertexAi().getProjectId(), properties.getVertexAi().getLocation());

    VertexAiTextEmbeddingModel cachedModel = (VertexAiTextEmbeddingModel) modelCache.get(cacheKey);

    if (cachedModel != null) {
      return cachedModel;
    }

    String apiKey = properties.getVertexAi().getApiKey();
    if (!StringUtils.hasText(apiKey)) {
      apiKey = springAiProperties.getVertexAi().getApiKey(); // Fallback to Spring AI property
      if (!StringUtils.hasText(apiKey)) {
        apiKey = System.getenv("VERTEX_AI_API_KEY"); // Fallback to environment variable

        if (!StringUtils.hasText(apiKey)) {
          apiKey = System.getProperty("SPRING_AI_VERTEX_AI_API_KEY"); // Fallback to system property
        }
      }
      properties.getVertexAi().setApiKey(apiKey);
    }

    String baseUrl = properties.getVertexAi().getEndpoint();
    if (!StringUtils.hasText(baseUrl)) {
      baseUrl = springAiProperties.getVertexAi().getEndpoint();
      properties.getVertexAi().setEndpoint(baseUrl);
    }

    String projectId = properties.getVertexAi().getProjectId();
    if (!StringUtils.hasText(projectId)) {
      projectId = springAiProperties.getVertexAi().getProjectId(); // Fallback to Spring AI property
      properties.getVertexAi().setProjectId(projectId);
    }

    String location = properties.getVertexAi().getLocation();
    if (!StringUtils.hasText(location)) {
      location = springAiProperties.getVertexAi().getLocation(); // Fallback to Spring AI property
      properties.getVertexAi().setLocation(location);
    }

    VertexAiEmbeddingConnectionDetails connectionDetails = VertexAiEmbeddingConnectionDetails.builder().projectId(
        properties.getVertexAi().getProjectId()).location(properties.getVertexAi().getLocation()).apiEndpoint(properties
            .getVertexAi().getEndpoint()).build();

    VertexAiTextEmbeddingOptions options = VertexAiTextEmbeddingOptions.builder().model(model).build();

    VertexAiTextEmbeddingModel embeddingModel = new VertexAiTextEmbeddingModel(connectionDetails, options);

    modelCache.put(cacheKey, embeddingModel);

    return embeddingModel;
  }

  /**
   * Creates or retrieves a cached Ollama embedding model.
   * 
   * <p>Ollama provides local LLM inference. The base URL must be configured
   * to point to the Ollama server instance.
   * 
   * @param model Model name available in Ollama (e.g., "llama2", "mistral")
   * @return Configured OllamaEmbeddingModel instance
   */
  public OllamaEmbeddingModel createOllamaEmbeddingModel(String model) {
    String cacheKey = generateCacheKey("ollama", model, properties.getOllama().getBaseUrl());

    OllamaEmbeddingModel cachedModel = (OllamaEmbeddingModel) modelCache.get(cacheKey);

    if (cachedModel != null) {
      return cachedModel;
    }

    OllamaApi api = OllamaApi.builder().baseUrl(properties.getOllama().getBaseUrl()).restClientBuilder(
        restClientBuilder).webClientBuilder(webClientBuilder).responseErrorHandler(responseErrorHandler).build();

    OllamaOptions options = OllamaOptions.builder().model(model).truncate(false).build();

    OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder().ollamaApi(api).defaultOptions(options).build();

    modelCache.put(cacheKey, embeddingModel);

    return embeddingModel;
  }

  /**
   * Retrieves AWS credentials for Bedrock services.
   * 
   * <p>Credentials are resolved from Redis OM properties with fallback to Spring AI properties.
   * 
   * @return AWS credentials for authentication
   */
  private AwsCredentials getAwsCredentials() {
    String accessKey = properties.getAws().getAccessKey();
    if (!StringUtils.hasText(accessKey)) {
      accessKey = springAiProperties.getBedrock().getAws().getAccessKey(); // Fallback to Spring AI property
      properties.getAws().setAccessKey(accessKey);
    }

    String secretKet = properties.getAws().getSecretKey();
    if (!StringUtils.hasText(secretKet)) {
      secretKet = springAiProperties.getBedrock().getAws().getSecretKey(); // Fallback to Spring AI property
      properties.getAws().setSecretKey(secretKet);
    }

    String region = properties.getAws().getRegion();
    if (!StringUtils.hasText(region)) {
      region = springAiProperties.getBedrock().getAws().getRegion(); // Fallback to Spring AI property
      properties.getAws().setRegion(region);
    }

    return AwsBasicCredentials.create(properties.getAws().getAccessKey(), properties.getAws().getSecretKey());
  }

  /**
   * Creates or retrieves a cached AWS Bedrock Cohere embedding model.
   * 
   * <p>Uses AWS credentials and region configuration to access Bedrock services.
   * 
   * @param model Cohere model identifier in Bedrock
   * @return Configured BedrockCohereEmbeddingModel instance
   */
  public BedrockCohereEmbeddingModel createCohereEmbeddingModel(String model) {
    String cacheKey = generateCacheKey("bedrock-cohere", model, properties.getAws().getAccessKey(), properties.getAws()
        .getSecretKey(), properties.getAws().getRegion(), String.valueOf(properties.getAws().getBedrockCohere()
            .getResponseTimeOut()));

    BedrockCohereEmbeddingModel cachedModel = (BedrockCohereEmbeddingModel) modelCache.get(cacheKey);

    if (cachedModel != null) {
      return cachedModel;
    }

    String region = properties.getAws().getRegion();
    if (!StringUtils.hasText(region)) {
      region = springAiProperties.getBedrock().getAws().getRegion(); // Fallback to Spring AI property
      properties.getAws().setRegion(region);
    }

    var cohereEmbeddingApi = new CohereEmbeddingBedrockApi(model, StaticCredentialsProvider.create(getAwsCredentials()),
        properties.getAws().getRegion(), ModelOptionsUtils.OBJECT_MAPPER, Duration.ofMinutes(properties.getAws()
            .getBedrockCohere().getResponseTimeOut()));

    BedrockCohereEmbeddingModel embeddingModel = new BedrockCohereEmbeddingModel(cohereEmbeddingApi);

    modelCache.put(cacheKey, embeddingModel);

    return embeddingModel;
  }

  /**
   * Creates or retrieves a cached AWS Bedrock Titan embedding model.
   * 
   * <p>Amazon Titan is AWS's native embedding model available through Bedrock.
   * 
   * @param model Titan model identifier in Bedrock
   * @return Configured BedrockTitanEmbeddingModel instance
   */
  public BedrockTitanEmbeddingModel createTitanEmbeddingModel(String model) {
    String cacheKey = generateCacheKey("bedrock-titan", model, properties.getAws().getAccessKey(), properties.getAws()
        .getSecretKey(), properties.getAws().getRegion(), String.valueOf(properties.getAws().getBedrockTitan()
            .getResponseTimeOut()));

    BedrockTitanEmbeddingModel cachedModel = (BedrockTitanEmbeddingModel) modelCache.get(cacheKey);

    if (cachedModel != null) {
      return cachedModel;
    }

    String region = properties.getAws().getRegion();
    if (!StringUtils.hasText(region)) {
      region = springAiProperties.getBedrock().getAws().getRegion(); // Fallback to Spring AI property
      properties.getAws().setRegion(region);
    }

    var titanEmbeddingApi = new TitanEmbeddingBedrockApi(model, StaticCredentialsProvider.create(getAwsCredentials()),
        properties.getAws().getRegion(), ModelOptionsUtils.OBJECT_MAPPER, Duration.ofMinutes(properties.getAws()
            .getBedrockTitan().getResponseTimeOut()));

    BedrockTitanEmbeddingModel embeddingModel = new BedrockTitanEmbeddingModel(titanEmbeddingApi, observationRegistry);

    modelCache.put(cacheKey, embeddingModel);

    return embeddingModel;
  }
}