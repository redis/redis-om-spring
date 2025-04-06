package com.redis.om.spring.vectorize;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Map;

@ConditionalOnProperty(name = "redis.om.spring.ai.enabled")
@ConfigurationProperties(prefix = "spring.ai")
public class SpringAiProperties {

  private OpenAi openai = new OpenAi();
  private AzureOpenAi azure = new AzureOpenAi();
  private Vertex vertex = new Vertex();
  private Bedrock bedrock = new Bedrock();
  private Transformers transformers = new Transformers();

  public OpenAi getOpenai() {
      return openai;
  }

  public void setOpenai(OpenAi openai) {
      this.openai = openai;
  }

  public AzureOpenAi getAzure() {
      return azure;
  }

  public void setAzure(AzureOpenAi azure) {
      this.azure = azure;
  }

  public Vertex.Ai.Gemini getVertexAi() {
      return vertex.getAi().getGemini();
  }

  public void setVertex(Vertex vertex) {
      this.vertex = vertex;
  }

  public Transformers getTransformers() {
      return transformers;
  }

  public void setTransformers(Transformers transformers) {
      this.transformers = transformers;
  }

  public Bedrock getBedrock() {
      return bedrock;
  }

  public void setBedrock(Bedrock bedrock) {
      this.bedrock = bedrock;
  }

  public static class OpenAi {
  private String apiKey;

  public String getApiKey() {
    if (!StringUtils.hasText(apiKey)) {
      apiKey = System.getenv("OPENAI_API_KEY"); // Fallback to environment variable

      if (!StringUtils.hasText(apiKey)) {
        apiKey = System.getProperty("SPRING_AI_OPENAI_API_KEY");  // Fallback to system property
      }
    }
    return apiKey;
  }

  public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
  }
}

  public static class AzureOpenAi {
    private String apiKey;
    private String endpoint;

      public String getApiKey() {
        if (!StringUtils.hasText(apiKey)) {
          apiKey = System.getenv("AZURE_OPENAI_API_KEY"); // Fallback to environment variable

          if (!StringUtils.hasText(apiKey)) {
            apiKey = System.getProperty("SPRING_AI_AZURE_OPENAI_API_KEY");  // Fallback to system property
          }
        }
        return apiKey;
      }

      public void setApiKey(String apiKey) {
          this.apiKey = apiKey;
      }

      public String getEndpoint() {
        if (!StringUtils.hasText(apiKey)) {
          endpoint = System.getenv("AZURE_OPENAI_ENDPOINT"); // Fallback to environment variable

          if (!StringUtils.hasText(endpoint)) {
            endpoint = System.getProperty("SPRING_AI_AZURE_OPENAI_ENDPOINT");  // Fallback to system property
          }
        }
        return endpoint;
      }

      public void setEndpoint(String endpoint) {
          this.endpoint = endpoint;
      }
  }

  public static class Vertex {
    private Ai ai = new Ai();

    public Ai getAi() {
        return ai;
    }

    public void setAi(Ai ai) {
        this.ai = ai;
    }

    public static class Ai {
      private Gemini gemini = new Gemini();

      public Gemini getGemini() {
          return gemini;
      }

      public void setGemini(Gemini gemini) {
          this.gemini = gemini;
      }

      public static class Gemini {
        private String apiKey;
        private String endpoint;
        private String projectId;
        private String location;

        public String getApiKey() {
          if (!StringUtils.hasText(apiKey)) {
            apiKey = System.getenv("VERTEX_AI_API_KEY"); // Fallback to environment variable
          }
          return apiKey;
        }

        public void setApiKey(String apiKey) {
          this.apiKey = apiKey;
        }

        public String getEndpoint() {
          return endpoint;
        }

        public void setEndpoint(String endpoint) {
          this.endpoint = endpoint;
        }

        public String getProjectId() {
          if (!StringUtils.hasText(projectId)) {
            projectId = System.getenv("VERTEX_AI_GEMINI_PROJECT_ID");
          }
          return projectId;
        }

        public void setProjectId(String projectId) {
          this.projectId = projectId;
        }

        public String getLocation() {
          if (!StringUtils.hasText(location)) {
            location = System.getenv("VERTEX_AI_GEMINI_LOCATION");
          }
          return location;
        }

        public void setLocation(String location) {
          this.location = location;
        }
      }
    }
  }

  public static class Bedrock {
    private Aws aws = new Aws();

    public Aws getAws() {
        return aws;
    }

    public void setAws(Aws aws) {
        this.aws = aws;
    }

    public static class Aws {
      private String region;
      private String accessKey;
      private String secretKey;

      public String getRegion() {
        if (!StringUtils.hasText(region)) {
          region = System.getProperty("aws.region");
          if (!StringUtils.hasText(region)) {
            region = System.getenv("AWS_REGION");
          }
        }
        return region;
      }

      public void setRegion(String region) {
        this.region = region;
      }

      public String getAccessKey() {
        if (!StringUtils.hasText(accessKey)) {
          accessKey = System.getProperty("aws.accessKeyId");
          if (!StringUtils.hasText(accessKey)) {
            accessKey = System.getenv("AWS_ACCESS_KEY_ID");
          }
        }
        return accessKey;
      }

      public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
      }

      public String getSecretKey() {
        if (!StringUtils.hasText(secretKey)) {
          secretKey = System.getProperty("aws.secretAccessKey");
          if (!StringUtils.hasText(secretKey)) {
            secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
          }
        }
        return secretKey;
      }

      public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
      }
    }
  }

  public static class Transformers {
    private String modelResource;
    private String tokenizerResource;
    private String resourceCacheDirectory;
    private Map<String, Object> tokenizerOptions;

    public String getModelResource() {
        return modelResource;
    }

    public void setModelResource(String modelResource) {
        this.modelResource = modelResource;
    }

    public String getTokenizerResource() {
        return tokenizerResource;
    }

    public void setTokenizerResource(String tokenizerResource) {
        this.tokenizerResource = tokenizerResource;
    }

    public String getResourceCacheDirectory() {
        return resourceCacheDirectory;
    }

    public void setResourceCacheDirectory(String resourceCacheDirectory) {
        this.resourceCacheDirectory = resourceCacheDirectory;
    }

    public Map<String, Object> getTokenizerOptions() {
        return tokenizerOptions;
    }

    public void setTokenizerOptions(Map<String, Object> tokenizerOptions) {
        this.tokenizerOptions = tokenizerOptions;
    }
  }
}