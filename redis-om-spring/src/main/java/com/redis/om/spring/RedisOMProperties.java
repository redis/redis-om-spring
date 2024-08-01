package com.redis.om.spring;

import jakarta.validation.constraints.NotNull;
import org.springframework.ai.openai.api.OpenAiApi.EmbeddingModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.geo.Metrics;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(
    prefix = "redis.om.spring", ignoreInvalidFields = true
)
public class RedisOMProperties {
  public static final String ROMS_VERSION = "0.9.5-SNAPSHOT";
  public static final int MAX_SEARCH_RESULTS = 10000;
  public static final double DEFAULT_DISTANCE = 0.0005;
  public static final Metrics DEFAULT_DISTANCE_METRIC = Metrics.MILES;
  // repository properties
  private final Repository repository = new Repository();
  private final References references = new References();
  private final Djl djl = new Djl();
  private final OpenAi openAi = new OpenAi();
  private final AzureOpenAi azureOpenAi = new AzureOpenAi();
  private final VertexAi vertexAi = new VertexAi();
  private final BedrockCohere bedrockCohere = new BedrockCohere();
  private final BedrockTitan bedrockTitan = new BedrockTitan();
  private final Ollama ollama = new Ollama();

  public Repository getRepository() {
    return repository;
  }

  public References getReferences() {
    return references;
  }

  public Djl getDjl() {
    return djl;
  }

  public OpenAi getOpenAi() {
    return openAi;
  }

  public AzureOpenAi getAzureOpenAi() {
    return azureOpenAi;
  }

  public VertexAi getVertexAi() {
    return vertexAi;
  }

  public BedrockCohere getBedrockCohere() {
    return bedrockCohere;
  }

  public BedrockTitan getBedrockTitan() {
    return bedrockTitan;
  }

  public Ollama getOllama() {
    return ollama;
  }

  public static class Repository {
    private final Query query = new Query();
    private boolean dropAndRecreateIndexOnDeleteAll = false;
    private int deleteBatchSize = 500;

    public Query getQuery() {
      return query;
    }

    public boolean isDropAndRecreateIndexOnDeleteAll() {
      return dropAndRecreateIndexOnDeleteAll;
    }

    public void setDropAndRecreateIndexOnDeleteAll(boolean dropAndRecreateIndexOnDeleteAll) {
      this.dropAndRecreateIndexOnDeleteAll = dropAndRecreateIndexOnDeleteAll;
    }

    public int getDeleteBatchSize() {
      return deleteBatchSize;
    }

    public void setDeleteBatchSize(int deleteBatchSize) {
      this.deleteBatchSize = deleteBatchSize;
    }

    public static class Query {
      private int limit = MAX_SEARCH_RESULTS;
      private double defaultDistance = DEFAULT_DISTANCE;
      private Metrics defaultDistanceMetric = DEFAULT_DISTANCE_METRIC;

      public int getLimit() {
        return limit;
      }

      public void setLimit(int limit) {
        this.limit = limit;
      }

      public double getDefaultDistance() {
        return defaultDistance;
      }

      public void setDefaultDistance(double defaultDistance) {
        this.defaultDistance = defaultDistance;
      }

      public Metrics getDefaultDistanceMetrics() {
        return defaultDistanceMetric;
      }

      public void setDefaultDistanceMetric(Metrics defaultDistanceMetric) {
        this.defaultDistanceMetric = defaultDistanceMetric;
      }
    }
  }

  public static class References {
    private String cacheName = "roms-reference-cache";
    private List<String> cachedReferenceClasses = new ArrayList<>();

    public String getCacheName() {
      return cacheName;
    }

    public void setCacheName(String cacheName) {
      this.cacheName = cacheName;
    }

    public List<String> getCachedReferenceClasses() {
      return cachedReferenceClasses;
    }

    public void setCachedReferenceClasses(List<String> cachedReferenceClasses) {
      this.cachedReferenceClasses = cachedReferenceClasses;
    }
  }

  // DJL properties
  public static class Djl {
    private static final String DEFAULT_ENGINE = "PyTorch";
    private boolean enabled = false;
    // image embedding settings
    @NotNull
    private String imageEmbeddingModelEngine = DEFAULT_ENGINE;
    @NotNull
    private String imageEmbeddingModelModelUrls = "djl://ai.djl.pytorch/resnet18_embedding";
    private int defaultImagePipelineResizeWidth = 224;
    private int defaultImagePipelineResizeHeight = 224;
    private boolean defaultImagePipelineCenterCrop = true;

    // sentence tokenizer settings
    @NotNull
    private String sentenceTokenizerMaxLength = "768";
    @NotNull
    private String sentenceTokenizerModelMaxLength = "768";
    @NotNull
    private String sentenceTokenizerModel = "sentence-transformers/all-mpnet-base-v2";

    // face detection
    @NotNull
    private String faceDetectionModelEngine = DEFAULT_ENGINE;
    @NotNull
    private String faceDetectionModelName = "retinaface";
    @NotNull
    private String faceDetectionModelModelUrls = "https://resources.djl.ai/test-models/pytorch/retinaface.zip";

    // face embeddings
    @NotNull
    private String faceEmbeddingModelEngine = DEFAULT_ENGINE;
    @NotNull
    private String faceEmbeddingModelName = "face_feature";
    @NotNull
    private String faceEmbeddingModelModelUrls = "https://resources.djl.ai/test-models/pytorch/face_feature.zip";

    public Djl() {
    }

    public boolean isEnabled() {
      return this.enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public @NotNull String getImageEmbeddingModelEngine() {
      return this.imageEmbeddingModelEngine;
    }

    public void setImageEmbeddingModelEngine(@NotNull String imageEmbeddingModelEngine) {
      this.imageEmbeddingModelEngine = imageEmbeddingModelEngine;
    }

    public @NotNull String getImageEmbeddingModelModelUrls() {
      return this.imageEmbeddingModelModelUrls;
    }

    public void setImageEmbeddingModelModelUrls(@NotNull String imageEmbeddingModelModelUrls) {
      this.imageEmbeddingModelModelUrls = imageEmbeddingModelModelUrls;
    }

    public int getDefaultImagePipelineResizeWidth() {
      return this.defaultImagePipelineResizeWidth;
    }

    public void setDefaultImagePipelineResizeWidth(int defaultImagePipelineResizeWidth) {
      this.defaultImagePipelineResizeWidth = defaultImagePipelineResizeWidth;
    }

    public int getDefaultImagePipelineResizeHeight() {
      return this.defaultImagePipelineResizeHeight;
    }

    public void setDefaultImagePipelineResizeHeight(int defaultImagePipelineResizeHeight) {
      this.defaultImagePipelineResizeHeight = defaultImagePipelineResizeHeight;
    }

    public boolean isDefaultImagePipelineCenterCrop() {
      return this.defaultImagePipelineCenterCrop;
    }

    public void setDefaultImagePipelineCenterCrop(boolean defaultImagePipelineCenterCrop) {
      this.defaultImagePipelineCenterCrop = defaultImagePipelineCenterCrop;
    }

    public @NotNull String getSentenceTokenizerMaxLength() {
      return this.sentenceTokenizerMaxLength;
    }

    public void setSentenceTokenizerMaxLength(@NotNull String sentenceTokenizerMaxLength) {
      this.sentenceTokenizerMaxLength = sentenceTokenizerMaxLength;
    }

    public @NotNull String getSentenceTokenizerModelMaxLength() {
      return this.sentenceTokenizerModelMaxLength;
    }

    public void setSentenceTokenizerModelMaxLength(@NotNull String sentenceTokenizerModelMaxLength) {
      this.sentenceTokenizerModelMaxLength = sentenceTokenizerModelMaxLength;
    }

    public @NotNull String getSentenceTokenizerModel() {
      return this.sentenceTokenizerModel;
    }

    public void setSentenceTokenizerModel(@NotNull String sentenceTokenizerModel) {
      this.sentenceTokenizerModel = sentenceTokenizerModel;
    }

    public @NotNull String getFaceDetectionModelEngine() {
      return this.faceDetectionModelEngine;
    }

    public void setFaceDetectionModelEngine(@NotNull String faceDetectionModelEngine) {
      this.faceDetectionModelEngine = faceDetectionModelEngine;
    }

    public @NotNull String getFaceDetectionModelName() {
      return this.faceDetectionModelName;
    }

    public void setFaceDetectionModelName(@NotNull String faceDetectionModelName) {
      this.faceDetectionModelName = faceDetectionModelName;
    }

    public @NotNull String getFaceDetectionModelModelUrls() {
      return this.faceDetectionModelModelUrls;
    }

    public void setFaceDetectionModelModelUrls(@NotNull String faceDetectionModelModelUrls) {
      this.faceDetectionModelModelUrls = faceDetectionModelModelUrls;
    }

    public @NotNull String getFaceEmbeddingModelEngine() {
      return this.faceEmbeddingModelEngine;
    }

    public void setFaceEmbeddingModelEngine(@NotNull String faceEmbeddingModelEngine) {
      this.faceEmbeddingModelEngine = faceEmbeddingModelEngine;
    }

    public @NotNull String getFaceEmbeddingModelName() {
      return this.faceEmbeddingModelName;
    }

    public void setFaceEmbeddingModelName(@NotNull String faceEmbeddingModelName) {
      this.faceEmbeddingModelName = faceEmbeddingModelName;
    }

    public @NotNull String getFaceEmbeddingModelModelUrls() {
      return this.faceEmbeddingModelModelUrls;
    }

    public void setFaceEmbeddingModelModelUrls(@NotNull String faceEmbeddingModelModelUrls) {
      this.faceEmbeddingModelModelUrls = faceEmbeddingModelModelUrls;
    }

    public String toString() {
      return "RedisOMSpringProperties.Djl(enabled=" + this.isEnabled() + ", imageEmbeddingModelEngine=" + this.getImageEmbeddingModelEngine() + ", imageEmbeddingModelModelUrls=" + this.getImageEmbeddingModelModelUrls() + ", defaultImagePipelineResizeWidth=" + this.getDefaultImagePipelineResizeWidth() + ", defaultImagePipelineResizeHeight=" + this.getDefaultImagePipelineResizeHeight() + ", defaultImagePipelineCenterCrop=" + this.isDefaultImagePipelineCenterCrop() + ", sentenceTokenizerMaxLength=" + this.getSentenceTokenizerMaxLength() + ", sentenceTokenizerModelMaxLength=" + this.getSentenceTokenizerModelMaxLength() + ", sentenceTokenizerModel=" + this.getSentenceTokenizerModel() + ", faceDetectionModelEngine=" + this.getFaceDetectionModelEngine() + ", faceDetectionModelName=" + this.getFaceDetectionModelName() + ", faceDetectionModelModelUrls=" + this.getFaceDetectionModelModelUrls() + ", faceEmbeddingModelEngine=" + this.getFaceEmbeddingModelEngine() + ", faceEmbeddingModelName=" + this.getFaceEmbeddingModelName() + ", faceEmbeddingModelModelUrls=" + this.getFaceEmbeddingModelModelUrls() + ")";
    }
  }

  public static class OpenAi {
    private String apiKey;
    private String model = EmbeddingModel.TEXT_EMBEDDING_ADA_002.getValue();

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }
  }

  public static class Ollama {
    private String baseUrl = "http://localhost:11434";

    public String getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }
  }

  public static class AzureOpenAi {
    private String apiKey;
    private String endPoint;
    private String model;

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    public String getEndPoint() {
      return endPoint;
    }

    public void setEndPoint(String endPoint) {
      this.endPoint = endPoint;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }
  }

  public static class VertexAi {
    private String apiKey;
    private String endPoint;
    private String model;

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    public String getEndPoint() {
      return endPoint;
    }

    public void setEndPoint(String endPoint) {
      this.endPoint = endPoint;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }
  }

  public static class BedrockCohere {
    private String region;
    private String accessKey;
    private String secretKey;
    private String model;

    public String getRegion() {
      return region;
    }

    public void setRegion(String region) {
      this.region = region;
    }

    public String getAccessKey() {
      return accessKey;
    }

    public void setAccessKey(String accessKey) {
      this.accessKey = accessKey;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public void setSecretKey(String secretKey) {
      this.secretKey = secretKey;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }
  }

  public static class BedrockTitan {
    private String region;
    private String accessKey;
    private String secretKey;
    private String model;

    public String getRegion() {
      return region;
    }

    public void setRegion(String region) {
      this.region = region;
    }

    public String getAccessKey() {
      return accessKey;
    }

    public void setAccessKey(String accessKey) {
      this.accessKey = accessKey;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public void setSecretKey(String secretKey) {
      this.secretKey = secretKey;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }
  }

}
