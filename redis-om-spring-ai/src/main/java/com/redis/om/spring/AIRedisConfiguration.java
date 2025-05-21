package com.redis.om.spring;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.redis.om.spring.vectorize.DefaultEmbedder;
import com.redis.om.spring.vectorize.Embedder;
import com.redis.om.spring.vectorize.EmbeddingModelFactory;
import com.redis.om.spring.vectorize.SpringAiProperties;
import com.redis.om.spring.vectorize.face.FaceDetectionTranslator;
import com.redis.om.spring.vectorize.face.FaceFeatureTranslator;

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
import io.micrometer.observation.ObservationRegistry;

@Configuration
@EnableConfigurationProperties(
  { AIRedisOMProperties.class }
)
@ConditionalOnProperty(
    name = "redis.om.spring.ai.enabled", havingValue = "true", matchIfMissing = false
)
public class AIRedisConfiguration {

  private static final Log logger = LogFactory.getLog(AIRedisConfiguration.class);

  //    @Value("${spring.data.redis.host:localhost}")
  //    private String host;
  //    @Value("${spring.data.redis.port:6379}")
  //    private int port;
  //    @Value("${spring.data.redis.username}")
  //    private String username;
  //    @Value("${spring.data.redis.password}")
  //    private String password;
  //
  ////    @Bean
  ////    @Primary
  ////    public JedisConnectionFactory redisConnectionFactory() {
  ////        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
  ////        redisStandaloneConfiguration.setHostName(host);
  ////        redisStandaloneConfiguration.setPort(port);
  ////        redisStandaloneConfiguration.setUsername(username);
  ////        redisStandaloneConfiguration.setPassword(password);
  ////        JedisConnectionFactory jediConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
  ////        jediConnectionFactory.setConvertPipelineAndTxResults(false);
  ////        return jediConnectionFactory;
  ////    }
  ////
  ////    @Bean
  ////    public RedisTemplate<String, String> redisTemplate() {
  ////
  ////        RedisTemplate<String, String> template = new RedisTemplate<>();
  ////        template.setConnectionFactory(redisConnectionFactory());
  ////        return template;
  ////    }

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }

  @Bean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }

  @Bean
  public ResponseErrorHandler defaultResponseErrorHandler() {
    return new DefaultResponseErrorHandler();
  }

  @Bean
  public ObservationRegistry observationRegistry() {
    return ObservationRegistry.create();
  }

  @Bean
  public EmbeddingModelFactory embeddingModelFactory(AIRedisOMProperties properties,
      SpringAiProperties springAiProperties, RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder,
      ResponseErrorHandler responseErrorHandler, ObservationRegistry observationRegistry) {
    return new EmbeddingModelFactory(properties, springAiProperties, restClientBuilder, webClientBuilder,
        responseErrorHandler, observationRegistry);
  }

  @Bean(
      name = "djlImageFactory"
  )
  public ImageFactory imageFactory() {
    return ImageFactory.getInstance();
  }

  @Bean(
      name = "djlImageEmbeddingModelCriteria"
  )
  public Criteria<Image, float[]> imageEmbeddingModelCriteria(AIRedisOMProperties properties) {
    return Criteria.builder().setTypes(Image.class, float[].class) //
        .optEngine(properties.getDjl().getImageEmbeddingModelEngine())  //
        .optModelUrls(properties.getDjl().getImageEmbeddingModelModelUrls()) //
        .build();
  }

  @Bean(
      name = "djlFaceDetectionTranslator"
  )
  public Translator<Image, DetectedObjects> faceDetectionTranslator() {
    double confThresh = 0.85f;
    double nmsThresh = 0.45f;
    double[] variance = { 0.1f, 0.2f };
    int topK = 5000;
    int[][] scales = { { 16, 32 }, { 64, 128 }, { 256, 512 } };
    int[] steps = { 8, 16, 32 };
    return new FaceDetectionTranslator(confThresh, nmsThresh, variance, topK, scales, steps);
  }

  @Bean(
      name = "djlFaceDetectionModelCriteria"
  )
  public Criteria<Image, DetectedObjects> faceDetectionModelCriteria( //
      @Qualifier(
        "djlFaceDetectionTranslator"
      ) Translator<Image, DetectedObjects> translator, //
      AIRedisOMProperties properties) {

    return Criteria.builder().setTypes(Image.class, DetectedObjects.class) //
        .optModelUrls(properties.getDjl().getFaceDetectionModelModelUrls()) //
        .optModelName(properties.getDjl().getFaceDetectionModelName()) //
        .optTranslator(translator) //
        .optEngine(properties.getDjl().getFaceDetectionModelEngine()) //
        .build();
  }

  @Bean(
      name = "djlFaceDetectionModel"
  )
  public ZooModel<Image, DetectedObjects> faceDetectionModel(@Nullable @Qualifier(
    "djlFaceDetectionModelCriteria"
  ) Criteria<Image, DetectedObjects> criteria) {
    try {
      return criteria != null ? ModelZoo.loadModel(criteria) : null;
    } catch (IOException | ModelNotFoundException | MalformedModelException ex) {
      logger.warn("Error retrieving default DJL face detection model", ex);
      return null;
    }
  }

  @Bean(
      name = "djlFaceEmbeddingTranslator"
  )
  public Translator<Image, float[]> faceEmbeddingTranslator() {
    return new FaceFeatureTranslator();
  }

  @Bean(
      name = "djlFaceEmbeddingModelCriteria"
  )
  public Criteria<Image, float[]> faceEmbeddingModelCriteria( //
      @Qualifier(
        "djlFaceEmbeddingTranslator"
      ) Translator<Image, float[]> translator, //
      AIRedisOMProperties properties) {

    return Criteria.builder() //
        .setTypes(Image.class, float[].class) //
        .optModelUrls(properties.getDjl().getFaceEmbeddingModelModelUrls()) //
        .optModelName(properties.getDjl().getFaceEmbeddingModelName()) //
        .optTranslator(translator) //
        .optEngine(properties.getDjl().getFaceEmbeddingModelEngine()) //
        .build();
  }

  @Bean(
      name = "djlFaceEmbeddingModel"
  )
  public ZooModel<Image, float[]> faceEmbeddingModel(@Nullable @Qualifier(
    "djlFaceEmbeddingModelCriteria"
  ) Criteria<Image, float[]> criteria) {
    try {
      return criteria != null ? ModelZoo.loadModel(criteria) : null;
    } catch (Exception e) {
      logger.warn("Error retrieving default DJL face embeddings model", e);
      return null;
    }
  }

  @Bean(
      name = "djlImageEmbeddingModel"
  )
  public ZooModel<Image, float[]> imageModel(@Nullable @Qualifier(
    "djlImageEmbeddingModelCriteria"
  ) Criteria<Image, float[]> criteria) throws MalformedModelException, ModelNotFoundException, IOException {
    return criteria != null ? ModelZoo.loadModel(criteria) : null;
  }

  @Bean(
      name = "djlDefaultImagePipeline"
  )
  public Pipeline defaultImagePipeline(AIRedisOMProperties properties) {
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

  @Bean(
      name = "djlSentenceTokenizer"
  )
  public HuggingFaceTokenizer sentenceTokenizer(AIRedisOMProperties properties) {
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

  @Primary
  @Bean(
      name = "featureExtractor"
  )
  public Embedder featureExtractor(@Nullable @Qualifier(
    "djlImageEmbeddingModel"
  ) ZooModel<Image, float[]> imageEmbeddingModel, @Nullable @Qualifier(
    "djlFaceEmbeddingModel"
  ) ZooModel<Image, float[]> faceEmbeddingModel, @Nullable @Qualifier(
    "djlImageFactory"
  ) ImageFactory imageFactory, @Nullable @Qualifier(
    "djlDefaultImagePipeline"
  ) Pipeline defaultImagePipeline, AIRedisOMProperties properties, EmbeddingModelFactory embeddingModelFactory,
      ApplicationContext ac) {
    return new DefaultEmbedder(ac, embeddingModelFactory, imageEmbeddingModel, faceEmbeddingModel, imageFactory,
        defaultImagePipeline, properties);
  }
}