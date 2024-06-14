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
import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.GsonBuilder;
import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.Cuckoo;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.pds.BloomOperations;
import com.redis.om.spring.ops.pds.CuckooFilterOperations;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.serialization.gson.*;
import com.redis.om.spring.vectorize.DefaultEmbedder;
import com.redis.om.spring.vectorize.Embedder;
import com.redis.om.spring.vectorize.NoopEmbedder;
import com.redis.om.spring.vectorize.face.FaceDetectionTranslator;
import com.redis.om.spring.vectorize.face.FaceFeatureTranslator;
import org.apache.commons.lang3.ObjectUtils;
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
import org.springframework.ai.vertexai.palm2.VertexAiPaLm2EmbeddingModel;
import org.springframework.ai.vertexai.palm2.api.VertexAiPaLm2Api;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import redis.clients.jedis.bloom.CFReserveParams;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.net.InetAddress;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.redis.om.spring.util.ObjectUtils.getBeanDefinitionsFor;
import static com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ RedisProperties.class, RedisOMProperties.class })
@EnableAspectJAutoProxy
@ComponentScan("com.redis.om.spring.bloom")
@ComponentScan("com.redis.om.spring.cuckoo")
@ComponentScan("com.redis.om.spring.autocomplete")
@ComponentScan("com.redis.om.spring.metamodel")
@ComponentScan("com.redis.om.spring.util")
public class RedisModulesConfiguration {

  private static final Log logger = LogFactory.getLog(RedisModulesConfiguration.class);

  @Bean(name = "omGsonBuilder")
  public GsonBuilder gsonBuilder(List<GsonBuilderCustomizer> customizers) {

    GsonBuilder builder = new GsonBuilder();
    // Enable the spring.gson.* configuration in the configuration file
    customizers.forEach(c -> c.customize(builder));

    builder.registerTypeAdapter(Point.class, PointTypeAdapter.getInstance());
    builder.registerTypeAdapter(Date.class, DateTypeAdapter.getInstance());
    builder.registerTypeAdapter(LocalDate.class, LocalDateTypeAdapter.getInstance());
    builder.registerTypeAdapter(LocalDateTime.class, LocalDateTimeTypeAdapter.getInstance());
    builder.registerTypeAdapter(Ulid.class, UlidTypeAdapter.getInstance());
    builder.registerTypeAdapter(Instant.class, InstantTypeAdapter.getInstance());
    builder.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeAdapter());
    builder.registerTypeAdapter(YearMonth.class, new YearMonthTypeAdapter());

    builder.addSerializationExclusionStrategy(GsonReferencesSerializationExclusionStrategy.INSTANCE);

    return builder;
  }

  @Bean(name = "redisModulesClient")
  @Lazy
  RedisModulesClient redisModulesClient( //
      JedisConnectionFactory jedisConnectionFactory, //
      @Qualifier("omGsonBuilder") GsonBuilder builder) {
    return new RedisModulesClient(jedisConnectionFactory, builder);
  }

  @Bean(name = "redisModulesOperations")
  @Primary
  @ConditionalOnMissingBean
  @Lazy
  RedisModulesOperations<?> redisModulesOperations( //
      RedisModulesClient rmc, //
      StringRedisTemplate template, //
      @Qualifier("omGsonBuilder") GsonBuilder gsonBuilder) {
    return new RedisModulesOperations<>(rmc, template, gsonBuilder);
  }

  @Bean(name = "redisJSONOperations")
  JSONOperations<?> redisJSONOperations(RedisModulesOperations<?> redisModulesOperations) {
    return redisModulesOperations.opsForJSON();
  }

  @Bean(name = "redisBloomOperations")
  BloomOperations<?> redisBloomOperations(RedisModulesOperations<?> redisModulesOperations) {
    return redisModulesOperations.opsForBloom();
  }

  @Bean(name = "redisCuckooOperations")
  CuckooFilterOperations<?> redisCuckooFilterOperations(RedisModulesOperations<?> redisModulesOperations) {
    return redisModulesOperations.opsForCuckoFilter();
  }

  @Bean(name = "redisOmTemplate")
  @Primary
  public RedisTemplate<?, ?> redisTemplate(JedisConnectionFactory connectionFactory) {
    RedisTemplate<?, ?> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setDefaultSerializer(new StringRedisSerializer());
    template.setConnectionFactory(connectionFactory);

    return template;
  }

  @Bean(name = "rediSearchIndexer")
  public RediSearchIndexer redisearchIndexer(ApplicationContext ac, //
      RedisOMProperties properties, //
      @Qualifier("omGsonBuilder") GsonBuilder gsonBuilder) {

    return new RediSearchIndexer(ac, properties, gsonBuilder);
  }

  @Bean(name = "djlImageFactory")
  public ImageFactory imageFactory() {
    return ImageFactory.getInstance();
  }

  @Bean(name = "djlImageEmbeddingModelCriteria")
  public Criteria<Image, byte[]> imageEmbeddingModelCriteria(RedisOMProperties properties) {
    return properties.getDjl().isEnabled() ? Criteria.builder().setTypes(Image.class, byte[].class) //
        .optEngine(properties.getDjl().getImageEmbeddingModelEngine())  //
        .optModelUrls(properties.getDjl().getImageEmbeddingModelModelUrls()) //
        .build() : null;
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
      RedisOMProperties properties) {

    return properties.getDjl().isEnabled() ? Criteria.builder().setTypes(Image.class, DetectedObjects.class) //
        .optModelUrls(properties.getDjl().getFaceDetectionModelModelUrls()) //
        .optModelName(properties.getDjl().getFaceDetectionModelName()) //
        .optTranslator(translator) //
        .optEngine(properties.getDjl().getFaceDetectionModelEngine()) //
        .build() : null;
  }

  @Bean(name = "djlFaceDetectionModel")
  public ZooModel<Image, DetectedObjects> faceDetectionModel(
      @Nullable @Qualifier("djlFaceDetectionModelCriteria") Criteria<Image, DetectedObjects> criteria,
      RedisOMProperties properties) {
    try {
      return properties.getDjl().isEnabled() && (criteria != null) ? ModelZoo.loadModel(criteria) : null;
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
      RedisOMProperties properties) {

    return properties.getDjl().isEnabled() ? Criteria.builder() //
        .setTypes(Image.class, float[].class).optModelUrls(properties.getDjl().getFaceEmbeddingModelModelUrls()) //
        .optModelName(properties.getDjl().getFaceEmbeddingModelName()) //
        .optTranslator(translator) //
        .optEngine(properties.getDjl().getFaceEmbeddingModelEngine()) //
        .build() : null;
  }

  @Bean(name = "djlFaceEmbeddingModel")
  public ZooModel<Image, float[]> faceEmbeddingModel(
      @Nullable @Qualifier("djlFaceEmbeddingModelCriteria") Criteria<Image, float[]> criteria, //
      RedisOMProperties properties) {
    try {
      return properties.getDjl().isEnabled() && (criteria != null) ? ModelZoo.loadModel(criteria) : null;
    } catch (Exception e) {
      logger.warn("Error retrieving default DJL face embeddings model", e);
      return null;
    }
  }

  @Bean(name = "djlImageEmbeddingModel")
  public ZooModel<Image, byte[]> imageModel(
      @Nullable @Qualifier("djlImageEmbeddingModelCriteria") Criteria<Image, byte[]> criteria,
      RedisOMProperties properties) throws MalformedModelException, ModelNotFoundException, IOException {
    return properties.getDjl().isEnabled() && (criteria != null) ? ModelZoo.loadModel(criteria) : null;
  }

  @Bean(name = "djlDefaultImagePipeline")
  public Pipeline defaultImagePipeline(RedisOMProperties properties) {
    if (properties.getDjl().isEnabled()) {
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
    } else
      return null;
  }

  @Bean(name = "djlSentenceTokenizer")
  public HuggingFaceTokenizer sentenceTokenizer(RedisOMProperties properties) {
    if (properties.getDjl().isEnabled()) {
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
    } else
      return null;
  }

  @ConditionalOnMissingBean
  @Bean
  public OpenAiEmbeddingModel openAITextVectorizer(RedisOMProperties properties,
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
          OpenAiEmbeddingOptions.builder().withModel("text-embedding-ada-002").build(),
          RetryUtils.DEFAULT_RETRY_TEMPLATE);
    } else {
      return null;
    }
  }

  @ConditionalOnMissingBean
  @Bean
  public OpenAIClient azureOpenAIClient(RedisOMProperties properties, //
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
  VertexAiPaLm2EmbeddingModel vertexAiPaLm2EmbeddingModel(RedisOMProperties properties, //
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
      VertexAiPaLm2Api vertexAiApi = new VertexAiPaLm2Api(baseUrl, apiKey, VertexAiPaLm2Api.DEFAULT_GENERATE_MODEL,
          VertexAiPaLm2Api.DEFAULT_EMBEDDING_MODEL, RestClient.builder());
      return new VertexAiPaLm2EmbeddingModel(vertexAiApi);
    } else {
      return null;
    }
  }

  @ConditionalOnMissingBean
  @Bean
  BedrockCohereEmbeddingModel bedrockCohereEmbeddingModel(RedisOMProperties properties, //
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
        model = CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V1.id();
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
  BedrockTitanEmbeddingModel bedrockTitanEmbeddingModel(RedisOMProperties properties, //
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

  @Bean(name = "featureExtractor")
  public Embedder featureExtractor(
      @Nullable @Qualifier("djlImageEmbeddingModel") ZooModel<Image, byte[]> imageEmbeddingModel,
      @Nullable @Qualifier("djlFaceEmbeddingModel") ZooModel<Image, float[]> faceEmbeddingModel,
      @Nullable @Qualifier("djlImageFactory") ImageFactory imageFactory,
      @Nullable @Qualifier("djlDefaultImagePipeline") Pipeline defaultImagePipeline,
      @Nullable @Qualifier("djlSentenceTokenizer") HuggingFaceTokenizer sentenceTokenizer,
      @Nullable OpenAiEmbeddingModel openAITextVectorizer, @Nullable OpenAIClient azureOpenAIClient,
      @Nullable VertexAiPaLm2EmbeddingModel vertexAiPaLm2EmbeddingModel,
      @Nullable BedrockCohereEmbeddingModel bedrockCohereEmbeddingModel,
      @Nullable BedrockTitanEmbeddingModel bedrockTitanEmbeddingModel, RedisOMProperties properties,
      ApplicationContext ac) {
    return properties.getDjl().isEnabled() ?
        new DefaultEmbedder(ac, imageEmbeddingModel, faceEmbeddingModel, imageFactory, defaultImagePipeline,
            sentenceTokenizer, openAITextVectorizer, azureOpenAIClient, vertexAiPaLm2EmbeddingModel,
            bedrockCohereEmbeddingModel, bedrockTitanEmbeddingModel, properties) :
        new NoopEmbedder();
  }

  @Bean(name = "redisJSONKeyValueAdapter")
  RedisJSONKeyValueAdapter getRedisJSONKeyValueAdapter( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> redisModulesOperations, //
      RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      @Qualifier("omGsonBuilder") GsonBuilder gsonBuilder, //
      RedisOMProperties properties, //
      @Nullable @Qualifier("featureExtractor") Embedder embedder) {
    return new RedisJSONKeyValueAdapter(redisOps, redisModulesOperations, mappingContext, indexer, gsonBuilder,
        embedder, properties);
  }

  @Bean(name = "redisJSONKeyValueTemplate")
  public CustomRedisKeyValueTemplate getRedisJSONKeyValueTemplate( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> redisModulesOperations, //
      RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      @Qualifier("omGsonBuilder") GsonBuilder gsonBuilder, //
      RedisOMProperties properties, //
      @Nullable @Qualifier("featureExtractor") Embedder embedder) {
    return new CustomRedisKeyValueTemplate(
        new RedisJSONKeyValueAdapter(redisOps, redisModulesOperations, mappingContext, indexer, gsonBuilder, embedder,
            properties), mappingContext);
  }

  @Bean(name = "redisCustomKeyValueTemplate")
  public CustomRedisKeyValueTemplate getKeyValueTemplate( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> redisModulesOperations, //
      RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      RedisOMProperties properties, //
      @Nullable @Qualifier("featureExtractor") Embedder embedder) {
    return new CustomRedisKeyValueTemplate(
        new RedisEnhancedKeyValueAdapter(redisOps, redisModulesOperations, mappingContext, indexer, embedder,
            properties), //
        mappingContext);
  }

  @Bean(name = "streamingQueryBuilder")
  EntityStream streamingQueryBuilder(RedisModulesOperations<?> redisModulesOperations,
      @Qualifier("omGsonBuilder") GsonBuilder gsonBuilder, RediSearchIndexer indexer) {
    return new EntityStreamImpl(redisModulesOperations, gsonBuilder, indexer);
  }

  @Bean(name = "redisOMCacheManager")
  public CacheManager getCacheManager() {
    return new ConcurrentMapCacheManager();
  }

  @EventListener(ContextRefreshedEvent.class)
  public void ensureIndexesAreCreated(ContextRefreshedEvent cre) {
    logger.info("Creating Indexes......");

    ApplicationContext ac = cre.getApplicationContext();

    RediSearchIndexer indexer = (RediSearchIndexer) ac.getBean("rediSearchIndexer");
    indexer.createIndicesFor(Document.class);
    indexer.createIndicesFor(RedisHash.class);
  }

  @EventListener(ContextRefreshedEvent.class)
  public void processBloom(ContextRefreshedEvent cre) {
    ApplicationContext ac = cre.getApplicationContext();
    @SuppressWarnings("unchecked") RedisModulesOperations<String> rmo = (RedisModulesOperations<String>) ac.getBean(
        "redisModulesOperations");

    Set<BeanDefinition> beanDefs = getBeanDefinitionsFor(ac, Document.class, RedisHash.class);

    for (BeanDefinition beanDef : beanDefs) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        for (java.lang.reflect.Field field : getDeclaredFieldsTransitively(cl)) {
          if (field.isAnnotationPresent(Bloom.class)) {
            Bloom bloom = field.getAnnotation(Bloom.class);
            BloomOperations<String> ops = rmo.opsForBloom();
            String filterName = !ObjectUtils.isEmpty(bloom.name()) ?
                bloom.name() :
                String.format("bf:%s:%s", cl.getSimpleName(), field.getName());
            ops.createFilter(filterName, bloom.capacity(), bloom.errorRate());
          }
        }
      } catch (Exception e) {
        logger.debug("Error during processing of @Bloom annotation: ", e);
      }
    }
  }

  @EventListener(ContextRefreshedEvent.class)
  public void processCuckoo(ContextRefreshedEvent cre) {
    ApplicationContext ac = cre.getApplicationContext();
    @SuppressWarnings("unchecked") RedisModulesOperations<String> rmo = (RedisModulesOperations<String>) ac.getBean(
        "redisModulesOperations");

    Set<BeanDefinition> beanDefs = getBeanDefinitionsFor(ac, Document.class, RedisHash.class);

    for (BeanDefinition beanDef : beanDefs) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        for (java.lang.reflect.Field field : getDeclaredFieldsTransitively(cl)) {
          if (field.isAnnotationPresent(Cuckoo.class)) {
            Cuckoo cuckoo = field.getAnnotation(Cuckoo.class);
            CuckooFilterOperations<String> ops = rmo.opsForCuckoFilter();
            String filterName = !ObjectUtils.isEmpty(cuckoo.name()) ?
                cuckoo.name() :
                String.format("cf:%s:%s", cl.getSimpleName(), field.getName());
            CFReserveParams params = CFReserveParams.reserveParams().bucketSize(cuckoo.bucketSize())
                .expansion(cuckoo.expansion()).maxIterations(cuckoo.maxIterations());
            ops.createFilter(filterName, cuckoo.capacity(), params);
          }
        }
      } catch (Exception e) {
        logger.debug("Error during processing of @Bloom annotation: ", e);
      }
    }
  }

  @EventListener(ContextRefreshedEvent.class)
  public void registerReferenceSerializer(ContextRefreshedEvent cre) {
    logger.info("Registering Reference Serializers......");

    ApplicationContext ac = cre.getApplicationContext();
    GsonBuilder gsonBuilder = (GsonBuilder) ac.getBean("omGsonBuilder");
    GsonReferenceSerializerRegistrar registrar = new GsonReferenceSerializerRegistrar(gsonBuilder, ac);

    registrar.registerReferencesFor(Document.class);
    registrar.registerReferencesFor(RedisHash.class);
  }
}
