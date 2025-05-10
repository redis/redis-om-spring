package com.redis.om.spring.vectorize;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.redis.om.spring.AIRedisOMProperties;
import com.redis.om.spring.annotations.Vectorize;
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
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class EmbeddingModelFactory {
    private final AIRedisOMProperties properties;
    private final SpringAiProperties springAiProperties;

    public EmbeddingModelFactory(AIRedisOMProperties properties, SpringAiProperties springAiProperties) {
        this.properties = properties;
        this.springAiProperties = springAiProperties;
    }

    public TransformersEmbeddingModel createTransformersEmbeddingModel(Vectorize vectorize) {
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
            Map<String, String> options = Arrays.stream(vectorize.transformersTokenizerOptions())
                    .map(entry -> entry.split("=", 2))
                    .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
            embeddingModel.setTokenizerOptions(options);
        }

        try {
            embeddingModel.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing TransformersEmbeddingModel", e);
        }

        return embeddingModel;
    }

    public OpenAiEmbeddingModel createOpenAiEmbeddingModel(EmbeddingModel model) {
        return createOpenAiEmbeddingModel(model.value);
    }

    public OpenAiEmbeddingModel createOpenAiEmbeddingModel(String model) {
        String apiKey = properties.getOpenAi().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            apiKey = springAiProperties.getOpenai().getApiKey();
            properties.getOpenAi().setApiKey(apiKey);
        }

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(properties.getOpenAi().getResponseTimeOut()));

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(properties.getOpenAi().getApiKey())
                .restClientBuilder(RestClient.builder().requestFactory(factory))
                .build();

        return new OpenAiEmbeddingModel(
                openAiApi,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .model(model)
                        .build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE
        );
    }

    private OpenAIClient getOpenAIClient() {
        OpenAIClientBuilder builder = new OpenAIClientBuilder();
        if (properties.getAzure().getEntraId().isEnabled()) {
            builder.credential(new DefaultAzureCredentialBuilder().tenantId(properties.getAzure().getEntraId().getTenantId()).build())
                    .endpoint(properties.getAzure().getEntraId().getEndpoint());
        } else {
            builder.credential(new AzureKeyCredential(properties.getAzure().getOpenAi().getApiKey()))
                    .endpoint(properties.getAzure().getOpenAi().getEndpoint());
        }
        return builder.buildClient();
    }

    public AzureOpenAiEmbeddingModel createAzureOpenAiEmbeddingModel(String deploymentName) {
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

        AzureOpenAiEmbeddingOptions options = AzureOpenAiEmbeddingOptions.builder()
                .deploymentName(deploymentName)
                .build();

        return new AzureOpenAiEmbeddingModel(openAIClient, MetadataMode.EMBED, options);
    }

    public VertexAiTextEmbeddingModel createVertexAiTextEmbeddingModel(String model) {
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

        VertexAiEmbeddingConnectionDetails connectionDetails = VertexAiEmbeddingConnectionDetails.builder()
                .projectId(properties.getVertexAi().getProjectId())
                .location(properties.getVertexAi().getLocation())
                .apiEndpoint(properties.getVertexAi().getEndpoint())
                .build();

        VertexAiTextEmbeddingOptions options = VertexAiTextEmbeddingOptions.builder()
                .model(model)
                .build();

        return new VertexAiTextEmbeddingModel(connectionDetails, options);
    }

    public OllamaEmbeddingModel createOllamaEmbeddingModel(String model) {
        OllamaApi api = new OllamaApi(properties.getOllama().getBaseUrl());

        OllamaOptions options = OllamaOptions.builder()
                .model(model)
                .truncate(false)
                .build();

        return OllamaEmbeddingModel.builder()
                .ollamaApi(api)
                .defaultOptions(options)
                .build();
    }

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

        return AwsBasicCredentials.create(
                properties.getAws().getAccessKey(),
                properties.getAws().getSecretKey()
        );
    }

    public BedrockCohereEmbeddingModel createCohereEmbeddingModel(String model) {
        String region = properties.getAws().getRegion();
        if (!StringUtils.hasText(region)) {
            region = springAiProperties.getBedrock().getAws().getRegion(); // Fallback to Spring AI property
            properties.getAws().setRegion(region);
        }

        var cohereEmbeddingApi = new CohereEmbeddingBedrockApi(
                model,
                StaticCredentialsProvider.create(getAwsCredentials()),
                properties.getAws().getRegion(),
                ModelOptionsUtils.OBJECT_MAPPER,
                Duration.ofMinutes(properties.getAws().getBedrockCohere().getResponseTimeOut())
        );

        return new BedrockCohereEmbeddingModel(cohereEmbeddingApi);
    }

    public BedrockTitanEmbeddingModel createTitanEmbeddingModel(String model) {
        String region = properties.getAws().getRegion();
        if (!StringUtils.hasText(region)) {
            region = springAiProperties.getBedrock().getAws().getRegion(); // Fallback to Spring AI property
            properties.getAws().setRegion(region);
        }

        var titanEmbeddingApi = new TitanEmbeddingBedrockApi(
                model,
                StaticCredentialsProvider.create(getAwsCredentials()),
                properties.getAws().getRegion(),
                ModelOptionsUtils.OBJECT_MAPPER,
                Duration.ofMinutes(properties.getAws().getBedrockTitan().getResponseTimeOut())
        );

        return new BedrockTitanEmbeddingModel(titanEmbeddingApi);
    }
}