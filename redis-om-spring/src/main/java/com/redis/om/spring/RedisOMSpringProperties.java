package com.redis.om.spring;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "redis.om.spring", ignoreInvalidFields = true
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

    // DJL properties
    public static class Djl {
        private static final String DEFAULT_ENGINE = "PyTorch";
        @NotNull
        private boolean enabled = false;
        // image embedding settings
        @NotNull
        private String imageEmbeddingModelEngine = DEFAULT_ENGINE;
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

        public @NotNull boolean isEnabled() {
            return this.enabled;
        }

        public @NotNull String getImageEmbeddingModelEngine() {
            return this.imageEmbeddingModelEngine;
        }

        public @NotNull String getImageEmbeddingModelModelUrls() {
            return this.imageEmbeddingModelModelUrls;
        }

        public @NotNull int getDefaultImagePipelineResizeWidth() {
            return this.defaultImagePipelineResizeWidth;
        }

        public @NotNull int getDefaultImagePipelineResizeHeight() {
            return this.defaultImagePipelineResizeHeight;
        }

        public @NotNull boolean isDefaultImagePipelineCenterCrop() {
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

        public void setEnabled(@NotNull boolean enabled) {
            this.enabled = enabled;
        }

        public void setImageEmbeddingModelEngine(@NotNull String imageEmbeddingModelEngine) {
            this.imageEmbeddingModelEngine = imageEmbeddingModelEngine;
        }

        public void setImageEmbeddingModelModelUrls(@NotNull String imageEmbeddingModelModelUrls) {
            this.imageEmbeddingModelModelUrls = imageEmbeddingModelModelUrls;
        }

        public void setDefaultImagePipelineResizeWidth(@NotNull int defaultImagePipelineResizeWidth) {
            this.defaultImagePipelineResizeWidth = defaultImagePipelineResizeWidth;
        }

        public void setDefaultImagePipelineResizeHeight(@NotNull int defaultImagePipelineResizeHeight) {
            this.defaultImagePipelineResizeHeight = defaultImagePipelineResizeHeight;
        }

        public void setDefaultImagePipelineCenterCrop(@NotNull boolean defaultImagePipelineCenterCrop) {
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

        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Djl))
                return false;
            final Djl other = (Djl) o;
            if (!other.canEqual((Object) this))
                return false;
            if (this.isEnabled() != other.isEnabled())
                return false;
            final Object this$imageEmbeddingModelEngine = this.getImageEmbeddingModelEngine();
            final Object other$imageEmbeddingModelEngine = other.getImageEmbeddingModelEngine();
            if (this$imageEmbeddingModelEngine == null ?
                other$imageEmbeddingModelEngine != null :
                !this$imageEmbeddingModelEngine.equals(other$imageEmbeddingModelEngine))
                return false;
            final Object this$imageEmbeddingModelModelUrls = this.getImageEmbeddingModelModelUrls();
            final Object other$imageEmbeddingModelModelUrls = other.getImageEmbeddingModelModelUrls();
            if (this$imageEmbeddingModelModelUrls == null ?
                other$imageEmbeddingModelModelUrls != null :
                !this$imageEmbeddingModelModelUrls.equals(other$imageEmbeddingModelModelUrls))
                return false;
            if (this.getDefaultImagePipelineResizeWidth() != other.getDefaultImagePipelineResizeWidth())
                return false;
            if (this.getDefaultImagePipelineResizeHeight() != other.getDefaultImagePipelineResizeHeight())
                return false;
            if (this.isDefaultImagePipelineCenterCrop() != other.isDefaultImagePipelineCenterCrop())
                return false;
            final Object this$sentenceTokenizerMaxLength = this.getSentenceTokenizerMaxLength();
            final Object other$sentenceTokenizerMaxLength = other.getSentenceTokenizerMaxLength();
            if (this$sentenceTokenizerMaxLength == null ?
                other$sentenceTokenizerMaxLength != null :
                !this$sentenceTokenizerMaxLength.equals(other$sentenceTokenizerMaxLength))
                return false;
            final Object this$sentenceTokenizerModelMaxLength = this.getSentenceTokenizerModelMaxLength();
            final Object other$sentenceTokenizerModelMaxLength = other.getSentenceTokenizerModelMaxLength();
            if (this$sentenceTokenizerModelMaxLength == null ?
                other$sentenceTokenizerModelMaxLength != null :
                !this$sentenceTokenizerModelMaxLength.equals(other$sentenceTokenizerModelMaxLength))
                return false;
            final Object this$sentenceTokenizerModel = this.getSentenceTokenizerModel();
            final Object other$sentenceTokenizerModel = other.getSentenceTokenizerModel();
            if (this$sentenceTokenizerModel == null ?
                other$sentenceTokenizerModel != null :
                !this$sentenceTokenizerModel.equals(other$sentenceTokenizerModel))
                return false;
            final Object this$faceDetectionModelEngine = this.getFaceDetectionModelEngine();
            final Object other$faceDetectionModelEngine = other.getFaceDetectionModelEngine();
            if (this$faceDetectionModelEngine == null ?
                other$faceDetectionModelEngine != null :
                !this$faceDetectionModelEngine.equals(other$faceDetectionModelEngine))
                return false;
            final Object this$faceDetectionModelName = this.getFaceDetectionModelName();
            final Object other$faceDetectionModelName = other.getFaceDetectionModelName();
            if (this$faceDetectionModelName == null ?
                other$faceDetectionModelName != null :
                !this$faceDetectionModelName.equals(other$faceDetectionModelName))
                return false;
            final Object this$faceDetectionModelModelUrls = this.getFaceDetectionModelModelUrls();
            final Object other$faceDetectionModelModelUrls = other.getFaceDetectionModelModelUrls();
            if (this$faceDetectionModelModelUrls == null ?
                other$faceDetectionModelModelUrls != null :
                !this$faceDetectionModelModelUrls.equals(other$faceDetectionModelModelUrls))
                return false;
            final Object this$faceEmbeddingModelEngine = this.getFaceEmbeddingModelEngine();
            final Object other$faceEmbeddingModelEngine = other.getFaceEmbeddingModelEngine();
            if (this$faceEmbeddingModelEngine == null ?
                other$faceEmbeddingModelEngine != null :
                !this$faceEmbeddingModelEngine.equals(other$faceEmbeddingModelEngine))
                return false;
            final Object this$faceEmbeddingModelName = this.getFaceEmbeddingModelName();
            final Object other$faceEmbeddingModelName = other.getFaceEmbeddingModelName();
            if (this$faceEmbeddingModelName == null ?
                other$faceEmbeddingModelName != null :
                !this$faceEmbeddingModelName.equals(other$faceEmbeddingModelName))
                return false;
            final Object this$faceEmbeddingModelModelUrls = this.getFaceEmbeddingModelModelUrls();
            final Object other$faceEmbeddingModelModelUrls = other.getFaceEmbeddingModelModelUrls();
            if (this$faceEmbeddingModelModelUrls == null ?
                other$faceEmbeddingModelModelUrls != null :
                !this$faceEmbeddingModelModelUrls.equals(other$faceEmbeddingModelModelUrls))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof Djl;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (this.isEnabled() ? 79 : 97);
            final Object $imageEmbeddingModelEngine = this.getImageEmbeddingModelEngine();
            result = result * PRIME + ($imageEmbeddingModelEngine == null ? 43 : $imageEmbeddingModelEngine.hashCode());
            final Object $imageEmbeddingModelModelUrls = this.getImageEmbeddingModelModelUrls();
            result = result * PRIME + ($imageEmbeddingModelModelUrls == null ?
                43 :
                $imageEmbeddingModelModelUrls.hashCode());
            result = result * PRIME + this.getDefaultImagePipelineResizeWidth();
            result = result * PRIME + this.getDefaultImagePipelineResizeHeight();
            result = result * PRIME + (this.isDefaultImagePipelineCenterCrop() ? 79 : 97);
            final Object $sentenceTokenizerMaxLength = this.getSentenceTokenizerMaxLength();
            result = result * PRIME + ($sentenceTokenizerMaxLength == null ?
                43 :
                $sentenceTokenizerMaxLength.hashCode());
            final Object $sentenceTokenizerModelMaxLength = this.getSentenceTokenizerModelMaxLength();
            result = result * PRIME + ($sentenceTokenizerModelMaxLength == null ?
                43 :
                $sentenceTokenizerModelMaxLength.hashCode());
            final Object $sentenceTokenizerModel = this.getSentenceTokenizerModel();
            result = result * PRIME + ($sentenceTokenizerModel == null ? 43 : $sentenceTokenizerModel.hashCode());
            final Object $faceDetectionModelEngine = this.getFaceDetectionModelEngine();
            result = result * PRIME + ($faceDetectionModelEngine == null ? 43 : $faceDetectionModelEngine.hashCode());
            final Object $faceDetectionModelName = this.getFaceDetectionModelName();
            result = result * PRIME + ($faceDetectionModelName == null ? 43 : $faceDetectionModelName.hashCode());
            final Object $faceDetectionModelModelUrls = this.getFaceDetectionModelModelUrls();
            result = result * PRIME + ($faceDetectionModelModelUrls == null ?
                43 :
                $faceDetectionModelModelUrls.hashCode());
            final Object $faceEmbeddingModelEngine = this.getFaceEmbeddingModelEngine();
            result = result * PRIME + ($faceEmbeddingModelEngine == null ? 43 : $faceEmbeddingModelEngine.hashCode());
            final Object $faceEmbeddingModelName = this.getFaceEmbeddingModelName();
            result = result * PRIME + ($faceEmbeddingModelName == null ? 43 : $faceEmbeddingModelName.hashCode());
            final Object $faceEmbeddingModelModelUrls = this.getFaceEmbeddingModelModelUrls();
            result = result * PRIME + ($faceEmbeddingModelModelUrls == null ?
                43 :
                $faceEmbeddingModelModelUrls.hashCode());
            return result;
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
