package com.redis.om.spring;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "redis.om.spring", ignoreInvalidFields = true
)
public class RedisOMProperties {
    public static int MAX_SEARCH_RESULTS = 10000;
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
            private int limit = MAX_SEARCH_RESULTS;

            public int getLimit() {
                return limit;
            }

            public void setLimit(int limit) {
                this.limit = limit;
            }
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

        public @NotNull String getImageEmbeddingModelEngine() {
            return this.imageEmbeddingModelEngine;
        }

        public @NotNull String getImageEmbeddingModelModelUrls() {
            return this.imageEmbeddingModelModelUrls;
        }

        public int getDefaultImagePipelineResizeWidth() {
            return this.defaultImagePipelineResizeWidth;
        }

        public int getDefaultImagePipelineResizeHeight() {
            return this.defaultImagePipelineResizeHeight;
        }

        public boolean isDefaultImagePipelineCenterCrop() {
            return this.defaultImagePipelineCenterCrop;
        }

        public @NotNull String getSentenceTokenizerMaxLength() {
            return this.sentenceTokenizerMaxLength;
        }

        public @NotNull String getSentenceTokenizerModelMaxLength() {
            return this.sentenceTokenizerModelMaxLength;
        }

        public @NotNull String getSentenceTokenizerModel() {
            return this.sentenceTokenizerModel;
        }

        public @NotNull String getFaceDetectionModelEngine() {
            return this.faceDetectionModelEngine;
        }

        public @NotNull String getFaceDetectionModelName() {
            return this.faceDetectionModelName;
        }

        public @NotNull String getFaceDetectionModelModelUrls() {
            return this.faceDetectionModelModelUrls;
        }

        public @NotNull String getFaceEmbeddingModelEngine() {
            return this.faceEmbeddingModelEngine;
        }

        public @NotNull String getFaceEmbeddingModelName() {
            return this.faceEmbeddingModelName;
        }

        public @NotNull String getFaceEmbeddingModelModelUrls() {
            return this.faceEmbeddingModelModelUrls;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setImageEmbeddingModelEngine(@NotNull String imageEmbeddingModelEngine) {
            this.imageEmbeddingModelEngine = imageEmbeddingModelEngine;
        }

        public void setImageEmbeddingModelModelUrls(@NotNull String imageEmbeddingModelModelUrls) {
            this.imageEmbeddingModelModelUrls = imageEmbeddingModelModelUrls;
        }

        public void setDefaultImagePipelineResizeWidth(int defaultImagePipelineResizeWidth) {
            this.defaultImagePipelineResizeWidth = defaultImagePipelineResizeWidth;
        }

        public void setDefaultImagePipelineResizeHeight(int defaultImagePipelineResizeHeight) {
            this.defaultImagePipelineResizeHeight = defaultImagePipelineResizeHeight;
        }

        public void setDefaultImagePipelineCenterCrop(boolean defaultImagePipelineCenterCrop) {
            this.defaultImagePipelineCenterCrop = defaultImagePipelineCenterCrop;
        }

        public void setSentenceTokenizerMaxLength(@NotNull String sentenceTokenizerMaxLength) {
            this.sentenceTokenizerMaxLength = sentenceTokenizerMaxLength;
        }

        public void setSentenceTokenizerModelMaxLength(@NotNull String sentenceTokenizerModelMaxLength) {
            this.sentenceTokenizerModelMaxLength = sentenceTokenizerModelMaxLength;
        }

        public void setSentenceTokenizerModel(@NotNull String sentenceTokenizerModel) {
            this.sentenceTokenizerModel = sentenceTokenizerModel;
        }

        public void setFaceDetectionModelEngine(@NotNull String faceDetectionModelEngine) {
            this.faceDetectionModelEngine = faceDetectionModelEngine;
        }

        public void setFaceDetectionModelName(@NotNull String faceDetectionModelName) {
            this.faceDetectionModelName = faceDetectionModelName;
        }

        public void setFaceDetectionModelModelUrls(@NotNull String faceDetectionModelModelUrls) {
            this.faceDetectionModelModelUrls = faceDetectionModelModelUrls;
        }

        public void setFaceEmbeddingModelEngine(@NotNull String faceEmbeddingModelEngine) {
            this.faceEmbeddingModelEngine = faceEmbeddingModelEngine;
        }

        public void setFaceEmbeddingModelName(@NotNull String faceEmbeddingModelName) {
            this.faceEmbeddingModelName = faceEmbeddingModelName;
        }

        public void setFaceEmbeddingModelModelUrls(@NotNull String faceEmbeddingModelModelUrls) {
            this.faceEmbeddingModelModelUrls = faceEmbeddingModelModelUrls;
        }

        public String toString() {
            return "RedisOMSpringProperties.Djl(enabled=" + this.isEnabled() + ", imageEmbeddingModelEngine=" + this.getImageEmbeddingModelEngine() + ", imageEmbeddingModelModelUrls=" + this.getImageEmbeddingModelModelUrls() + ", defaultImagePipelineResizeWidth=" + this.getDefaultImagePipelineResizeWidth() + ", defaultImagePipelineResizeHeight=" + this.getDefaultImagePipelineResizeHeight() + ", defaultImagePipelineCenterCrop=" + this.isDefaultImagePipelineCenterCrop() + ", sentenceTokenizerMaxLength=" + this.getSentenceTokenizerMaxLength() + ", sentenceTokenizerModelMaxLength=" + this.getSentenceTokenizerModelMaxLength() + ", sentenceTokenizerModel=" + this.getSentenceTokenizerModel() + ", faceDetectionModelEngine=" + this.getFaceDetectionModelEngine() + ", faceDetectionModelName=" + this.getFaceDetectionModelName() + ", faceDetectionModelModelUrls=" + this.getFaceDetectionModelModelUrls() + ", faceEmbeddingModelEngine=" + this.getFaceEmbeddingModelEngine() + ", faceEmbeddingModelName=" + this.getFaceEmbeddingModelName() + ", faceEmbeddingModelModelUrls=" + this.getFaceEmbeddingModelModelUrls() + ")";
        }
    }

    private final Djl djl = new Djl();

    public Djl getDjl() {
        return djl;
    }
}
