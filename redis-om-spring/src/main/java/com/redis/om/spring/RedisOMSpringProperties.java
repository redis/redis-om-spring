package com.redis.om.spring;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.translate.Pipeline;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

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
    }

    private final Djl djl = new Djl();

    public Djl getDjl() {
        return djl;
    }
}
