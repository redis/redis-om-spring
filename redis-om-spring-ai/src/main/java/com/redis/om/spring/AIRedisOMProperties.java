package com.redis.om.spring;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "redis.om.spring.ai", ignoreInvalidFields = true
)
@ConditionalOnProperty(name = "redis.om.spring.ai.enabled", havingValue = "true",  matchIfMissing = false)
public class AIRedisOMProperties {
    private boolean enabled = false;
    private int embeddingBatchSize = 1000;
    private final Djl djl = new Djl();
    private final OpenAi openAi = new OpenAi();
    private final AzureClients azure = new AzureClients();
    private final VertexAi vertexAi = new VertexAi();
    private final Aws aws = new Aws();
    private final Ollama ollama = new Ollama();

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Djl getDjl() {
        return djl;
    }

    public OpenAi getOpenAi() {
        return openAi;
    }

    public AzureClients getAzure() {
        return azure;
    }

    public VertexAi getVertexAi() {
        return vertexAi;
    }

    public Aws getAws() {
        return aws;
    }

    public Ollama getOllama() {
        return ollama;
    }

    public int getEmbeddingBatchSize() {
        return embeddingBatchSize;
    }

    public void setEmbeddingBatchSize(int embeddingBatchSize) {
        this.embeddingBatchSize = embeddingBatchSize;
    }

    public static class Djl {
        private static final String DEFAULT_ENGINE = "PyTorch";
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
        private String sentenceTokenizerModel = "sentence-transformers/msmarco-distilbert-dot-v5";

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
            return "RedisOMSpringProperties.Ai.Djl(imageEmbeddingModelEngine=" + this.getImageEmbeddingModelEngine() + ", imageEmbeddingModelModelUrls=" + this.getImageEmbeddingModelModelUrls() + ", defaultImagePipelineResizeWidth=" + this.getDefaultImagePipelineResizeWidth() + ", defaultImagePipelineResizeHeight=" + this.getDefaultImagePipelineResizeHeight() + ", defaultImagePipelineCenterCrop=" + this.isDefaultImagePipelineCenterCrop() + ", sentenceTokenizerMaxLength=" + this.getSentenceTokenizerMaxLength() + ", sentenceTokenizerModelMaxLength=" + this.getSentenceTokenizerModelMaxLength() + ", sentenceTokenizerModel=" + this.getSentenceTokenizerModel() + ", faceDetectionModelEngine=" + this.getFaceDetectionModelEngine() + ", faceDetectionModelName=" + this.getFaceDetectionModelName() + ", faceDetectionModelModelUrls=" + this.getFaceDetectionModelModelUrls() + ", faceEmbeddingModelEngine=" + this.getFaceEmbeddingModelEngine() + ", faceEmbeddingModelName=" + this.getFaceEmbeddingModelName() + ", faceEmbeddingModelModelUrls=" + this.getFaceEmbeddingModelModelUrls() + ")";
        }
    }

    public static class OpenAi {
        private String apiKey;
        private long responseTimeOut = 60;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public long getResponseTimeOut() {
            return responseTimeOut;
        }

        public void setResponseTimeOut(long responseTimeOut) {
            this.responseTimeOut = responseTimeOut;
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

    public static class AzureClients {
        private AzureOpenAi openAi;
        private AzureEntraId entraId;

        public AzureOpenAi getOpenAi() {
            return openAi;
        }

        public void setOpenAi(AzureOpenAi openAi) {
            this.openAi = openAi;
        }

        public AzureEntraId getEntraId() {
            return entraId;
        }

        public void setEntraId(AzureEntraId entraId) {
            this.entraId = entraId;
        }
    }

    public static class AzureOpenAi {
        private String apiKey;
        private String endpoint;

        public String getApiKey() {
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
    }

    public static class AzureEntraId {
        private boolean enabled = false;
        private String apiKey;
        private String endpoint;
        private String tenantId;
        private String clientId;
        private String clientSecret;

        public String getApiKey() {
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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }

    public static class VertexAi {
        private String apiKey;
        private String endpoint;
        private String projectId;
        private String location;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getApiKey() {
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
    }

    public static class Aws {
        private String region;
        private String accessKey;
        private String secretKey;
        private BedrockCohere bedrockCohere = new BedrockCohere();
        private BedrockTitan bedrockTitan = new BedrockTitan();

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

        public BedrockCohere getBedrockCohere() {
            return bedrockCohere;
        }

        public void setBedrockCohere(BedrockCohere bedrockCohere) {
            this.bedrockCohere = bedrockCohere;
        }

        public BedrockTitan getBedrockTitan() {
            return bedrockTitan;
        }

        public void setBedrockTitan(BedrockTitan bedrockTitan) {
            this.bedrockTitan = bedrockTitan;
        }

        public static class BedrockCohere {
            private int responseTimeOut = 60;

            public int getResponseTimeOut() {
                return responseTimeOut;
            }

            public void setResponseTimeOut(int responseTimeOut) {
                this.responseTimeOut = responseTimeOut;
            }
        }

        public static class BedrockTitan {
            private long responseTimeOut = 300;

            public long getResponseTimeOut() {
                return responseTimeOut;
            }

            public void setResponseTimeOut(long responseTimeOut) {
                this.responseTimeOut = responseTimeOut;
            }
        }
    }
}
