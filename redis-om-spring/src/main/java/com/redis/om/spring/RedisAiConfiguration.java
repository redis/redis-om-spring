package com.redis.om.spring;

import ai.djl.MalformedModelException;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Pipeline;
import ai.djl.translate.Translator;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.redis.om.spring.vectorize.DefaultEmbedder;
import com.redis.om.spring.vectorize.Embedder;
import com.redis.om.spring.vectorize.face.FaceDetectionTranslator;
import com.redis.om.spring.vectorize.face.FaceFeatureTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingModel;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingModel;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingModel;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Map;

@ConditionalOnProperty(name = "redis.om.spring.ai.enabled")
@Configuration
@EnableConfigurationProperties({ RedisOMAiProperties.class })
public class RedisAiConfiguration {

  private static final Log logger = LogFactory.getLog(RedisAiConfiguration.class);

  @Bean(name = "djlImageFactory")
  public ImageFactory imageFactory() {
    return ImageFactory.getInstance();
  }

  @Bean(name = "djlImageEmbeddingModelCriteria")
  public Criteria<Image, float[]> imageEmbeddingModelCriteria(RedisOMAiProperties properties) {
    return Criteria.builder().setTypes(Image.class, float[].class) //
        .optEngine(properties.getDjl().getImageEmbeddingModelEngine())  //
        .optModelUrls(properties.getDjl().getImageEmbeddingModelModelUrls()) //
        .build();
  }

  @Bean(name = "djlFaceDetectionTranslator")
  public Translator<Image, DetectedObjects> faceDetectionTranslator() {
    double confThresh = 0.85f;
    double nmsThresh = 0.45f;
    double[] variance = { 0.1f, 0.2f };
    int topK = 5000;
    int[][] scales = { { 16, 32 }, { 64, 128 }, { 256, 512 } };
    int[] steps = { 8, 16, 32 };
    return new FaceDetectionTranslator(confThresh, nmsThresh, variance, topK, scales, steps);
  }

  @Bean(name = "djlFaceDetectionModelCriteria")
  public Criteria<Image, DetectedObjects> faceDetectionModelCriteria( //
      @Qualifier("djlFaceDetectionTranslator") Translator<Image, DetectedObjects> translator, //
      RedisOMAiProperties properties) {

    return Criteria.builder().setTypes(Image.class, DetectedObjects.class) //
        .optModelUrls(properties.getDjl().getFaceDetectionModelModelUrls()) //
        .optModelName(properties.getDjl().getFaceDetectionModelName()) //
        .optTranslator(translator) //
        .optEngine(properties.getDjl().getFaceDetectionModelEngine()) //
        .build();
  }

  @Bean(name = "djlFaceDetectionModel")
  public ZooModel<Image, DetectedObjects> faceDetectionModel(
      @Nullable @Qualifier("djlFaceDetectionModelCriteria") Criteria<Image, DetectedObjects> criteria) {
    try {
      return criteria != null ? ModelZoo.loadModel(criteria) : null;
    } catch (IOException | ModelNotFoundException | MalformedModelException ex) {
      logger.warn("Error retrieving default DJL face detection model", ex);
      return null;
    }
  }

  @Bean(name = "djlFaceEmbeddingTranslator")
  public Translator<Image, float[]> faceEmbeddingTranslator() {
    return new FaceFeatureTranslator();
  }

  @Bean(name = "djlFaceEmbeddingModelCriteria")
  public Criteria<Image, float[]> faceEmbeddingModelCriteria( //
      @Qualifier("djlFaceEmbeddingTranslator") Translator<Image, float[]> translator, //
      RedisOMAiProperties properties) {

    return Criteria.builder() //
        .setTypes(Image.class, float[].class) //
        .optModelUrls(properties.getDjl().getFaceEmbeddingModelModelUrls()) //
        .optModelName(properties.getDjl().getFaceEmbeddingModelName()) //
        .optTranslator(translator) //
        .optEngine(properties.getDjl().getFaceEmbeddingModelEngine()) //
        .build();
  }

  @Bean(name = "djlFaceEmbeddingModel")
  public ZooModel<Image, float[]> faceEmbeddingModel(
      @Nullable @Qualifier("djlFaceEmbeddingModelCriteria") Criteria<Image, float[]> criteria) {
    try {
      return criteria != null ? ModelZoo.loadModel(criteria) : null;
    } catch (Exception e) {
      logger.warn("Error retrieving default DJL face embeddings model", e);
      return null;
    }
  }

  @Bean(name = "djlImageEmbeddingModel")
  public ZooModel<Image, float[]> imageModel(
      @Nullable @Qualifier("djlImageEmbeddingModelCriteria") Criteria<Image, float[]> criteria)
      throws MalformedModelException, ModelNotFoundException, IOException {
    return criteria != null ? ModelZoo.loadModel(criteria) : null;
  }

  @Bean(name = "djlDefaultImagePipeline")
  public Pipeline defaultImagePipeline(RedisOMAiProperties properties) {
    Pipeline pipeline = new Pipeline();
    if (properties.getDjl().isDefaultImagePipelineCenterCrop()) {
      pipeline.add(new CenterCrop());
    }
    return pipeline //
            .add(new Resize( //
                    properties.getDjl().getDefaultImagePipelineResizeWidth(), //
                    properties.getDjl().getDefaultImagePipelineResizeHeight() //
            )) //
            .add(new ToTensor());
  }

  @Bean(name = "djlSentenceTokenizer")
  public HuggingFaceTokenizer sentenceTokenizer(RedisOMAiProperties properties) {
    Map<String, String> options = Map.of( //
            "maxLength", properties.getDjl().getSentenceTokenizerMaxLength(), //
            "modelMaxLength", properties.getDjl().getSentenceTokenizerModelMaxLength() //
    );

    try {
      //noinspection ResultOfMethodCallIgnored
      InetAddress.getByName("www.huggingface.co").isReachable(5000);
      return HuggingFaceTokenizer.newInstance(properties.getDjl().getSentenceTokenizerModel(), options);
    } catch (IOException ioe) {
      logger.warn("Error retrieving default DJL sentence tokenizer");
      return null;
    }
  }

  @Bean(name = "transformersEmbeddingModel")
  public TransformersEmbeddingModel transformersEmbeddingModel(RedisOMAiProperties properties) {
    TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
    if (properties.getTransformers().getTokenizerResource() != null) {
      embeddingModel.setTokenizerResource(properties.getTransformers().getTokenizerResource());
    }

    if (properties.getTransformers().getModelResource() != null) {
      embeddingModel.setModelResource(properties.getTransformers().getModelResource());
    }

    if (properties.getTransformers().getResourceCacheDirectory() != null) {
      embeddingModel.setResourceCacheDirectory(properties.getTransformers().getResourceCacheDirectory());
    }

    if (!properties.getTransformers().getTokenizerOptions().isEmpty()) {
      embeddingModel.setTokenizerOptions(properties.getTransformers().getTokenizerOptions());
    }

    return embeddingModel;
  }

  @ConditionalOnMissingBean
  @Bean
  public OpenAiEmbeddingModel openAITextVectorizer(RedisOMAiProperties properties,
      @Value("${spring.ai.openai.api-key:}") String apiKey) {
    if (!StringUtils.hasText(apiKey)) {
      apiKey = properties.getOpenAi().getApiKey();
      if (!StringUtils.hasText(apiKey)) {
        // Fallback to environment variable
        apiKey = System.getenv("OPENAI_API_KEY");

        if (!StringUtils.hasText(apiKey)) {
          // Fallback to system property
          apiKey = System.getProperty("SPRING_AI_OPENAI_API_KEY");
        }
      }
    }

    if (StringUtils.hasText(apiKey)) {
      properties.getOpenAi().setApiKey(apiKey);
    }

    if (StringUtils.hasText(apiKey)) {
      var openAiApi = new OpenAiApi(apiKey);

      // Rest of the configuration
      return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED,
          OpenAiEmbeddingOptions.builder().model("text-embedding-ada-002").build(),
          RetryUtils.DEFAULT_RETRY_TEMPLATE);
    } else {
      return null;
    }
  }

  @ConditionalOnMissingBean
  @Bean
  public OpenAIClient azureOpenAIClient(RedisOMAiProperties properties, //
      @Value("${spring.ai.azure.openai.api-key:}") String apiKey,
      @Value("${spring.ai.azure.openai.endpoint:}") String endpoint) {
    if (!StringUtils.hasText(apiKey)) {
      apiKey = properties.getAzureOpenAi().getApiKey();
      if (!StringUtils.hasText(apiKey)) {
        // Fallback to environment variable
        apiKey = System.getenv("AZURE_OPENAI_API_KEY");

        if (!StringUtils.hasText(apiKey)) {
          // Fallback to system property
          apiKey = System.getProperty("SPRING_AI_AZURE_OPENAI_API_KEY");
        }
      }
    }

    if (!StringUtils.hasText(endpoint)) {
      endpoint = properties.getAzureOpenAi().getEndPoint();
      if (!StringUtils.hasText(apiKey)) {
        // Fallback to environment variable
        endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");

        if (!StringUtils.hasText(apiKey)) {
          // Fallback to system property
          endpoint = System.getProperty("SPRING_AI_AZURE_OPENAI_ENDPOINT");
        }
      }
    }

    if (StringUtils.hasText(apiKey) && StringUtils.hasText(endpoint)) {
      return new OpenAIClientBuilder().credential(new AzureKeyCredential(apiKey)).endpoint(endpoint).buildClient();
    } else {
      return null;
    }
  }

  @ConditionalOnMissingBean
  @Bean
  VertexAiTextEmbeddingModel vertexAiEmbeddingModel(RedisOMAiProperties properties, //
      @Value("${spring.ai.vertex.ai.api-key:}") String apiKey,
      @Value("${spring.ai.vertex.ai.ai.base-url:}") String baseUrl) {
    if (!StringUtils.hasText(apiKey)) {
      apiKey = properties.getVertexAi().getApiKey();
      if (!StringUtils.hasText(apiKey)) {
        // Fallback to environment variable
        apiKey = System.getenv("VERTEX_AI_API_KEY");

        if (!StringUtils.hasText(apiKey)) {
          // Fallback to system property
          apiKey = System.getProperty("SPRING_AI_VERTEX_AI_API_KEY");
        }
      }
    }

    if (!StringUtils.hasText(baseUrl)) {
      baseUrl = properties.getVertexAi().getEndPoint();
      if (!StringUtils.hasText(apiKey)) {
        // Fallback to environment variable
        baseUrl = System.getenv("VERTEX_AI_ENDPOINT");

        if (!StringUtils.hasText(apiKey)) {
          // Fallback to system property
          baseUrl = System.getProperty("SPRING_AI_VERTEX_AI_ENDPOINT");
        }
      }
    }

    if (StringUtils.hasText(apiKey) && StringUtils.hasText(baseUrl)) {

      VertexAiEmbeddingConnectionDetails connectionDetails = VertexAiEmbeddingConnectionDetails.builder()
          .projectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID")).location(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
          .build();

      VertexAiTextEmbeddingOptions options = VertexAiTextEmbeddingOptions.builder()
          .model(VertexAiTextEmbeddingOptions.DEFAULT_MODEL_NAME).build();

      return new VertexAiTextEmbeddingModel(connectionDetails, options);
    } else {
      return null;
    }
  }

  @ConditionalOnMissingBean
  @Bean
  BedrockCohereEmbeddingModel bedrockCohereEmbeddingModel(RedisOMAiProperties properties, //
      @Value("${spring.ai.bedrock.aws.region:}") String region,
      @Value("${spring.ai.bedrock.aws.access-key:}") String accessKey,
      @Value("${spring.ai.bedrock.aws.secret-key:}") String secretKey,
      @Value("${spring.ai.bedrock.cohere.embedding.model:}") String model) {
    if (!StringUtils.hasText(region)) {
      region = properties.getBedrockCohere().getRegion();
      if (!StringUtils.hasText(region)) {
        // Fallback to environment variable
        region = System.getenv("AWS_REGION");

        if (!StringUtils.hasText(region)) {
          // Fallback to system property
          region = System.getProperty("SPRING_AI_AWS_REGION");
        }
        properties.getBedrockCohere().setRegion(region);
      }
    }
    if (!StringUtils.hasText(secretKey)) {
      region = Region.US_EAST_1.id();
    }

    if (!StringUtils.hasText(accessKey)) {
      accessKey = properties.getBedrockCohere().getAccessKey();
      if (!StringUtils.hasText(accessKey)) {
        // Fallback to environment variable
        accessKey = System.getenv("AWS_ACCESS_KEY_ID");

        if (!StringUtils.hasText(accessKey)) {
          // Fallback to system property
          accessKey = System.getProperty("SPRING_AI_AWS_ACCESS_KEY_ID");
        }

        properties.getBedrockCohere().setAccessKey(accessKey);
      }
    }

    if (!StringUtils.hasText(secretKey)) {
      secretKey = properties.getBedrockCohere().getSecretKey();
      if (!StringUtils.hasText(secretKey)) {
        // Fallback to environment variable
        secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");

        if (!StringUtils.hasText(secretKey)) {
          // Fallback to system property
          secretKey = System.getProperty("SPRING_AI_AWS_SECRET_ACCESS_KEY");
        }

        properties.getBedrockCohere().setSecretKey(secretKey);
      }
    }

    if (!StringUtils.hasText(model)) {
      model = properties.getBedrockCohere().getModel();
      if (!StringUtils.hasText(model)) {
        model = CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V3.id();
        properties.getBedrockCohere().setModel(model);
      }
    }

    if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
      AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
      var cohereEmbeddingApi = new CohereEmbeddingBedrockApi(model, StaticCredentialsProvider.create(credentials),
          region, ModelOptionsUtils.OBJECT_MAPPER);

      return new BedrockCohereEmbeddingModel(cohereEmbeddingApi);
    } else {
      return null;
    }
  }

  @ConditionalOnMissingBean
  @Bean
  BedrockTitanEmbeddingModel bedrockTitanEmbeddingModel(RedisOMAiProperties properties, //
      @Value("${spring.ai.bedrock.aws.region:}") String region,
      @Value("${spring.ai.bedrock.aws.access-key:}") String accessKey,
      @Value("${spring.ai.bedrock.aws.secret-key:}") String secretKey,
      @Value("${spring.ai.bedrock.titan.embedding.model:}") String model) {
    if (!StringUtils.hasText(region)) {
      region = properties.getBedrockCohere().getRegion();
      if (!StringUtils.hasText(region)) {
        // Fallback to environment variable
        region = System.getenv("AWS_REGION");

        if (!StringUtils.hasText(region)) {
          // Fallback to system property
          region = System.getProperty("SPRING_AI_AWS_REGION");
        }
        properties.getBedrockCohere().setRegion(region);
      }
    }
    if (!StringUtils.hasText(secretKey)) {
      region = Region.US_EAST_1.id();
    }

    if (!StringUtils.hasText(accessKey)) {
      accessKey = properties.getBedrockTitan().getAccessKey();
      if (!StringUtils.hasText(accessKey)) {
        // Fallback to environment variable
        accessKey = System.getenv("AWS_ACCESS_KEY_ID");

        if (!StringUtils.hasText(accessKey)) {
          // Fallback to system property
          accessKey = System.getProperty("SPRING_AI_AWS_ACCESS_KEY_ID");
        }

        properties.getBedrockTitan().setAccessKey(accessKey);
      }
    }

    if (!StringUtils.hasText(secretKey)) {
      secretKey = properties.getBedrockTitan().getSecretKey();
      if (!StringUtils.hasText(secretKey)) {
        // Fallback to environment variable
        secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");

        if (!StringUtils.hasText(secretKey)) {
          // Fallback to system property
          secretKey = System.getProperty("SPRING_AI_AWS_SECRET_ACCESS_KEY");
        }

        properties.getBedrockTitan().setSecretKey(secretKey);
      }
    }

    if (!StringUtils.hasText(model)) {
      model = properties.getBedrockTitan().getModel();
      if (!StringUtils.hasText(model)) {
        model = TitanEmbeddingModel.TITAN_EMBED_IMAGE_V1.id();
        properties.getBedrockTitan().setModel(model);
      }
    }

    if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
      AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

      var titanEmbeddingApi = new TitanEmbeddingBedrockApi(model, StaticCredentialsProvider.create(credentials), region,
          ModelOptionsUtils.OBJECT_MAPPER, Duration.ofMinutes(5L));

      return new BedrockTitanEmbeddingModel(titanEmbeddingApi);
    } else {
      return null;
    }
  }

  @Primary
  @Bean(name = "featureExtractor")
  public Embedder featureExtractor(
      @Nullable @Qualifier("djlImageEmbeddingModel") ZooModel<Image, float[]> imageEmbeddingModel,
      @Nullable @Qualifier("djlFaceEmbeddingModel") ZooModel<Image, float[]> faceEmbeddingModel,
      @Nullable @Qualifier("djlImageFactory") ImageFactory imageFactory,
      @Nullable @Qualifier("djlDefaultImagePipeline") Pipeline defaultImagePipeline,
      @Nullable @Qualifier("transformersEmbeddingModel") TransformersEmbeddingModel transformersEmbeddingModel,
      @Nullable OpenAiEmbeddingModel openAITextVectorizer, @Nullable OpenAIClient azureOpenAIClient,
      @Nullable VertexAiTextEmbeddingModel vertexAiTextEmbeddingModel,
      @Nullable BedrockCohereEmbeddingModel bedrockCohereEmbeddingModel,
      @Nullable BedrockTitanEmbeddingModel bedrockTitanEmbeddingModel,
      RedisOMAiProperties properties,
      ApplicationContext ac) {
    return new DefaultEmbedder(ac, imageEmbeddingModel, faceEmbeddingModel, imageFactory, defaultImagePipeline,
        transformersEmbeddingModel, openAITextVectorizer, azureOpenAIClient, vertexAiTextEmbeddingModel,
            bedrockCohereEmbeddingModel, bedrockTitanEmbeddingModel, properties);
  }
}
