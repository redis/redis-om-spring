package com.redis.om.spring;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "redis.om.spring",
        ignoreInvalidFields = true
)
public class RedisOMSpringProperties {
    // repository properties
    private final Repository repository = new Repository();

    public Repository getRepository() {
        return repository;
    }

    public static class Repository {
        private final Query query = new Query();

        public Query getQuery() {
            return query;
        }

        public static class Query {
            private int limit = 10000;

            public int getLimit() {
                return limit;
            }

            public void setLimit(int limit) {
                this.limit = limit;
            }
        }
    }

    // DJL properies
    @Data
    public static class Djl {
        // image embedding settings
        @NotNull
        private String imageEmbeddingModelEngine = "PyTorch";
        @NotNull
        private String imageEmbeddingModelModelUrls = "djl://ai.djl.pytorch/resnet18_embedding";
        @NotNull
        private int defaultImagePipelineResizeWidth = 224;
        @NotNull
        private int defaultImagePipelineResizeHeight = 224;
        @NotNull
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
        private String faceDetectionModelEngine = "PyTorch";
        @NotNull
        private String faceDetectionModelName = "retinaface";
        @NotNull
        private String faceDetectionModelModelUrls = "https://resources.djl.ai/test-models/pytorch/retinaface.zip";

        // face embeddings
        @NotNull
        private String faceEmbeddingModelEngine = "PyTorch";
        @NotNull
        private String faceEmbeddingModelName = "face_feature";
        @NotNull
        private String faceEmbeddingModelModelUrls = "https://resources.djl.ai/test-models/pytorch/face_feature.zip";
    }

    private final Djl djl = new Djl();

    public Djl getDjl() {
        return djl;
    }
}
