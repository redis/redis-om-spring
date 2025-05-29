package com.redis.om.spring.vectorize;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Configuration properties for Spring AI integration, providing fallback values for AI provider settings.
 * 
 * <p>This class captures Spring AI configuration properties that can be used as fallbacks when
 * Redis OM specific properties are not configured. It supports multiple AI providers including:
 * <ul>
 * <li>OpenAI - For GPT-based embeddings</li>
 * <li>Azure OpenAI - Microsoft's hosted OpenAI service</li>
 * <li>Vertex AI - Google Cloud's AI platform</li>
 * <li>AWS Bedrock - Amazon's managed AI service</li>
 * <li>Transformers - Local Hugging Face models</li>
 * </ul>
 * 
 * <p>Properties are resolved with multiple fallback mechanisms:
 * <ol>
 * <li>Application properties (spring.ai.*)</li>
 * <li>Environment variables</li>
 * <li>System properties</li>
 * </ol>
 * 
 * <p>This bean is only created when Redis OM Spring AI is enabled via
 * {@code redis.om.spring.ai.enabled=true}.
 * 
 * @see com.redis.om.spring.AIRedisOMProperties
 * @see EmbeddingModelFactory
 */
@ConditionalOnProperty(
    name = "redis.om.spring.ai.enabled", havingValue = "true"
)
@ConfigurationProperties(
    prefix = "spring.ai"
)
public class SpringAiProperties {

  /**
   * Default constructor for Spring configuration properties binding.
   * This constructor is used by Spring Boot's configuration properties mechanism
   * to create and populate instances of this class from application properties.
   */
  public SpringAiProperties() {
    // Default constructor
  }

  /** OpenAI configuration properties */
  private OpenAi openai = new OpenAi();

  /** Azure OpenAI configuration properties */
  private AzureOpenAi azure = new AzureOpenAi();

  /** Google Vertex AI configuration properties */
  private Vertex vertex = new Vertex();

  /** AWS Bedrock configuration properties */
  private Bedrock bedrock = new Bedrock();

  /** Transformers (local models) configuration properties */
  private Transformers transformers = new Transformers();

  /**
   * Gets OpenAI configuration properties.
   * 
   * @return OpenAI configuration
   */
  public OpenAi getOpenai() {
    return openai;
  }

  /**
   * Sets OpenAI configuration properties.
   * 
   * @param openai OpenAI configuration
   */
  public void setOpenai(OpenAi openai) {
    this.openai = openai;
  }

  /**
   * Gets Azure OpenAI configuration properties.
   * 
   * @return Azure OpenAI configuration
   */
  public AzureOpenAi getAzure() {
    return azure;
  }

  /**
   * Sets Azure OpenAI configuration properties.
   * 
   * @param azure Azure OpenAI configuration
   */
  public void setAzure(AzureOpenAi azure) {
    this.azure = azure;
  }

  /**
   * Gets Vertex AI Gemini configuration properties.
   * 
   * @return Vertex AI Gemini configuration
   */
  public Vertex.Ai.Gemini getVertexAi() {
    return vertex.getAi().getGemini();
  }

  /**
   * Sets Vertex AI configuration properties.
   * 
   * @param vertex Vertex AI configuration
   */
  public void setVertex(Vertex vertex) {
    this.vertex = vertex;
  }

  /**
   * Gets Transformers configuration properties.
   * 
   * @return Transformers configuration
   */
  public Transformers getTransformers() {
    return transformers;
  }

  /**
   * Sets Transformers configuration properties.
   * 
   * @param transformers Transformers configuration
   */
  public void setTransformers(Transformers transformers) {
    this.transformers = transformers;
  }

  /**
   * Gets AWS Bedrock configuration properties.
   * 
   * @return Bedrock configuration
   */
  public Bedrock getBedrock() {
    return bedrock;
  }

  /**
   * Sets AWS Bedrock configuration properties.
   * 
   * @param bedrock Bedrock configuration
   */
  public void setBedrock(Bedrock bedrock) {
    this.bedrock = bedrock;
  }

  /**
   * Configuration properties for OpenAI integration.
   */
  public static class OpenAi {
    /**
     * Default constructor for Spring configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public OpenAi() {
      // Default constructor for configuration properties binding
    }

    /** API key for OpenAI authentication */
    private String apiKey;

    /**
     * Gets the OpenAI API key with fallback resolution.
     * 
     * <p>Resolution order:
     * <ol>
     * <li>Configured property value</li>
     * <li>OPENAI_API_KEY environment variable</li>
     * <li>SPRING_AI_OPENAI_API_KEY system property</li>
     * </ol>
     * 
     * @return OpenAI API key or null if not configured
     */
    public String getApiKey() {
      if (!StringUtils.hasText(apiKey)) {
        apiKey = System.getenv("OPENAI_API_KEY"); // Fallback to environment variable

        if (!StringUtils.hasText(apiKey)) {
          apiKey = System.getProperty("SPRING_AI_OPENAI_API_KEY");  // Fallback to system property
        }
      }
      return apiKey;
    }

    /**
     * Sets the OpenAI API key.
     * 
     * @param apiKey OpenAI API key
     */
    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }
  }

  /**
   * Configuration properties for Azure OpenAI integration.
   */
  public static class AzureOpenAi {
    /**
     * Default constructor for Spring configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public AzureOpenAi() {
      // Default constructor for configuration properties binding
    }

    /** API key for Azure OpenAI authentication */
    private String apiKey;

    /** Azure OpenAI endpoint URL */
    private String endpoint;

    /**
     * Gets the Azure OpenAI API key with fallback resolution.
     * 
     * <p>Resolution order:
     * <ol>
     * <li>Configured property value</li>
     * <li>AZURE_OPENAI_API_KEY environment variable</li>
     * <li>SPRING_AI_AZURE_OPENAI_API_KEY system property</li>
     * </ol>
     * 
     * @return Azure OpenAI API key or null if not configured
     */
    public String getApiKey() {
      if (!StringUtils.hasText(apiKey)) {
        apiKey = System.getenv("AZURE_OPENAI_API_KEY"); // Fallback to environment variable

        if (!StringUtils.hasText(apiKey)) {
          apiKey = System.getProperty("SPRING_AI_AZURE_OPENAI_API_KEY");  // Fallback to system property
        }
      }
      return apiKey;
    }

    /**
     * Sets the Azure OpenAI API key.
     * 
     * @param apiKey Azure OpenAI API key
     */
    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    /**
     * Gets the Azure OpenAI endpoint with fallback resolution.
     * 
     * <p>Resolution order:
     * <ol>
     * <li>Configured property value</li>
     * <li>AZURE_OPENAI_ENDPOINT environment variable</li>
     * <li>SPRING_AI_AZURE_OPENAI_ENDPOINT system property</li>
     * </ol>
     * 
     * @return Azure OpenAI endpoint URL or null if not configured
     */
    public String getEndpoint() {
      if (!StringUtils.hasText(apiKey)) {
        endpoint = System.getenv("AZURE_OPENAI_ENDPOINT"); // Fallback to environment variable

        if (!StringUtils.hasText(endpoint)) {
          endpoint = System.getProperty("SPRING_AI_AZURE_OPENAI_ENDPOINT");  // Fallback to system property
        }
      }
      return endpoint;
    }

    /**
     * Sets the Azure OpenAI endpoint.
     * 
     * @param endpoint Azure OpenAI endpoint URL
     */
    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }
  }

  /**
   * Configuration properties for Google Vertex AI.
   */
  public static class Vertex {
    /**
     * Default constructor for Spring configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public Vertex() {
      // Default constructor for configuration properties binding
    }

    /** AI service configuration */
    private Ai ai = new Ai();

    /**
     * Gets AI service configuration.
     * 
     * @return AI configuration
     */
    public Ai getAi() {
      return ai;
    }

    /**
     * Sets AI service configuration.
     * 
     * @param ai AI configuration
     */
    public void setAi(Ai ai) {
      this.ai = ai;
    }

    /**
     * AI service configuration container.
     */
    public static class Ai {
      /**
       * Default constructor for Spring configuration properties binding.
       * This constructor is used by Spring Boot's configuration properties mechanism
       * to create and populate instances of this class from application properties.
       */
      public Ai() {
        // Default constructor for configuration properties binding
      }

      /** Gemini model configuration */
      private Gemini gemini = new Gemini();

      /**
       * Gets Gemini model configuration.
       * 
       * @return Gemini configuration
       */
      public Gemini getGemini() {
        return gemini;
      }

      /**
       * Sets Gemini model configuration.
       * 
       * @param gemini Gemini configuration
       */
      public void setGemini(Gemini gemini) {
        this.gemini = gemini;
      }

      /**
       * Configuration properties for Google Gemini models in Vertex AI.
       */
      public static class Gemini {
        /**
         * Default constructor for Spring configuration properties binding.
         * This constructor is used by Spring Boot's configuration properties mechanism
         * to create and populate instances of this class from application properties.
         */
        public Gemini() {
          // Default constructor for configuration properties binding
        }

        /** API key for Vertex AI authentication */
        private String apiKey;

        /** Vertex AI endpoint URL */
        private String endpoint;

        /** Google Cloud project ID */
        private String projectId;

        /** Google Cloud region/location */
        private String location;

        /**
         * Gets the Vertex AI API key with environment variable fallback.
         * 
         * @return Vertex AI API key or null if not configured
         */
        public String getApiKey() {
          if (!StringUtils.hasText(apiKey)) {
            apiKey = System.getenv("VERTEX_AI_API_KEY"); // Fallback to environment variable
          }
          return apiKey;
        }

        /**
         * Sets the Vertex AI API key.
         * 
         * @param apiKey Vertex AI API key
         */
        public void setApiKey(String apiKey) {
          this.apiKey = apiKey;
        }

        /**
         * Gets the Vertex AI endpoint URL.
         * 
         * @return Vertex AI endpoint
         */
        public String getEndpoint() {
          return endpoint;
        }

        /**
         * Sets the Vertex AI endpoint URL.
         * 
         * @param endpoint Vertex AI endpoint
         */
        public void setEndpoint(String endpoint) {
          this.endpoint = endpoint;
        }

        /**
         * Gets the Google Cloud project ID with environment variable fallback.
         * 
         * @return Project ID or null if not configured
         */
        public String getProjectId() {
          if (!StringUtils.hasText(projectId)) {
            projectId = System.getenv("VERTEX_AI_GEMINI_PROJECT_ID");
          }
          return projectId;
        }

        /**
         * Sets the Google Cloud project ID.
         * 
         * @param projectId Google Cloud project ID
         */
        public void setProjectId(String projectId) {
          this.projectId = projectId;
        }

        /**
         * Gets the Google Cloud location with environment variable fallback.
         * 
         * @return Location/region or null if not configured
         */
        public String getLocation() {
          if (!StringUtils.hasText(location)) {
            location = System.getenv("VERTEX_AI_GEMINI_LOCATION");
          }
          return location;
        }

        /**
         * Sets the Google Cloud location.
         * 
         * @param location Google Cloud location/region
         */
        public void setLocation(String location) {
          this.location = location;
        }
      }
    }
  }

  /**
   * Configuration properties for AWS Bedrock AI services.
   */
  public static class Bedrock {
    /**
     * Default constructor for Spring configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public Bedrock() {
      // Default constructor for configuration properties binding
    }

    /** AWS configuration */
    private Aws aws = new Aws();

    /**
     * Gets AWS configuration.
     * 
     * @return AWS configuration
     */
    public Aws getAws() {
      return aws;
    }

    /**
     * Sets AWS configuration.
     * 
     * @param aws AWS configuration
     */
    public void setAws(Aws aws) {
      this.aws = aws;
    }

    /**
     * AWS credentials and configuration for Bedrock.
     */
    public static class Aws {
      /**
       * Default constructor for Spring configuration properties binding.
       * This constructor is used by Spring Boot's configuration properties mechanism
       * to create and populate instances of this class from application properties.
       */
      public Aws() {
        // Default constructor for configuration properties binding
      }

      /** AWS region */
      private String region;

      /** AWS access key ID */
      private String accessKey;

      /** AWS secret access key */
      private String secretKey;

      /**
       * Gets AWS region with fallback resolution.
       * 
       * <p>Resolution order:
       * <ol>
       * <li>Configured property value</li>
       * <li>aws.region system property</li>
       * <li>AWS_REGION environment variable</li>
       * </ol>
       * 
       * @return AWS region or null if not configured
       */
      public String getRegion() {
        if (!StringUtils.hasText(region)) {
          region = System.getProperty("aws.region");
          if (!StringUtils.hasText(region)) {
            region = System.getenv("AWS_REGION");
          }
        }
        return region;
      }

      /**
       * Sets AWS region.
       * 
       * @param region AWS region
       */
      public void setRegion(String region) {
        this.region = region;
      }

      /**
       * Gets AWS access key with fallback resolution.
       * 
       * <p>Resolution order:
       * <ol>
       * <li>Configured property value</li>
       * <li>aws.accessKeyId system property</li>
       * <li>AWS_ACCESS_KEY_ID environment variable</li>
       * </ol>
       * 
       * @return AWS access key or null if not configured
       */
      public String getAccessKey() {
        if (!StringUtils.hasText(accessKey)) {
          accessKey = System.getProperty("aws.accessKeyId");
          if (!StringUtils.hasText(accessKey)) {
            accessKey = System.getenv("AWS_ACCESS_KEY_ID");
          }
        }
        return accessKey;
      }

      /**
       * Sets AWS access key.
       * 
       * @param accessKey AWS access key ID
       */
      public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
      }

      /**
       * Gets AWS secret key with fallback resolution.
       * 
       * <p>Resolution order:
       * <ol>
       * <li>Configured property value</li>
       * <li>aws.secretAccessKey system property</li>
       * <li>AWS_SECRET_ACCESS_KEY environment variable</li>
       * </ol>
       * 
       * @return AWS secret key or null if not configured
       */
      public String getSecretKey() {
        if (!StringUtils.hasText(secretKey)) {
          secretKey = System.getProperty("aws.secretAccessKey");
          if (!StringUtils.hasText(secretKey)) {
            secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
          }
        }
        return secretKey;
      }

      /**
       * Sets AWS secret key.
       * 
       * @param secretKey AWS secret access key
       */
      public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
      }
    }
  }

  /**
   * Configuration properties for Hugging Face Transformers models.
   * 
   * <p>Transformers models run locally and can be loaded from Hugging Face hub
   * or local filesystem.
   */
  public static class Transformers {
    /**
     * Default constructor for Spring configuration properties binding.
     * This constructor is used by Spring Boot's configuration properties mechanism
     * to create and populate instances of this class from application properties.
     */
    public Transformers() {
      // Default constructor for configuration properties binding
    }

    /** Path or URL to the model resource */
    private String modelResource;

    /** Path or URL to the tokenizer resource */
    private String tokenizerResource;

    /** Directory for caching downloaded resources */
    private String resourceCacheDirectory;

    /** Additional tokenizer configuration options */
    private Map<String, Object> tokenizerOptions;

    /**
     * Gets the model resource path or URL.
     * 
     * @return Model resource location
     */
    public String getModelResource() {
      return modelResource;
    }

    /**
     * Sets the model resource path or URL.
     * 
     * @param modelResource Model resource location
     */
    public void setModelResource(String modelResource) {
      this.modelResource = modelResource;
    }

    /**
     * Gets the tokenizer resource path or URL.
     * 
     * @return Tokenizer resource location
     */
    public String getTokenizerResource() {
      return tokenizerResource;
    }

    /**
     * Sets the tokenizer resource path or URL.
     * 
     * @param tokenizerResource Tokenizer resource location
     */
    public void setTokenizerResource(String tokenizerResource) {
      this.tokenizerResource = tokenizerResource;
    }

    /**
     * Gets the resource cache directory.
     * 
     * @return Cache directory path
     */
    public String getResourceCacheDirectory() {
      return resourceCacheDirectory;
    }

    /**
     * Sets the resource cache directory.
     * 
     * @param resourceCacheDirectory Cache directory path
     */
    public void setResourceCacheDirectory(String resourceCacheDirectory) {
      this.resourceCacheDirectory = resourceCacheDirectory;
    }

    /**
     * Gets tokenizer configuration options.
     * 
     * @return Map of tokenizer options
     */
    public Map<String, Object> getTokenizerOptions() {
      return tokenizerOptions;
    }

    /**
     * Sets tokenizer configuration options.
     * 
     * @param tokenizerOptions Map of tokenizer options
     */
    public void setTokenizerOptions(Map<String, Object> tokenizerOptions) {
      this.tokenizerOptions = tokenizerOptions;
    }
  }
}