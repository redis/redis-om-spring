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

/**
 * Spring configuration class for Redis OM AI features.
 * This configuration is automatically enabled when the property 'redis.om.spring.ai.enabled'
 * is set to true. It sets up various AI-related beans including embedding models,
 * vector databases, and document readers for different AI providers such as OpenAI,
 * Azure OpenAI, Ollama, and others.
 * 
 * <p>The configuration provides support for:</p>
 * <ul>
 * <li>OpenAI embedding models and chat models</li>
 * <li>Azure OpenAI integration</li>
 * <li>Ollama local models</li>
 * <li>Qdrant vector database</li>
 * <li>Document processing and text readers</li>
 * <li>Image embedding with DJL models</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(
  { AIRedisOMProperties.class }
)
@ConditionalOnProperty(
    name = "redis.om.spring.ai.enabled", havingValue = "true", matchIfMissing = false
)
public class AIRedisConfiguration {

  /**
   * Default constructor for Spring instantiation.
   * This constructor is used by Spring Framework to create instances of this configuration class.
   */
  public AIRedisConfiguration() {
    // Default constructor
  }

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

  /**
   * Creates a RestClient.Builder bean for building REST clients.
   * This builder is used throughout the AI configuration for making HTTP requests
   * to various AI providers.
   *
   * @return a new RestClient.Builder instance
   */
  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }

  /**
   * Creates a WebClient.Builder bean for building reactive web clients.
   * This builder is used for asynchronous HTTP requests to AI providers
   * that support reactive programming models.
   *
   * @return a new WebClient.Builder instance
   */
  @Bean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }

  /**
   * Creates a default ResponseErrorHandler bean for handling HTTP response errors.
   * This handler is used by REST clients to process error responses from AI providers.
   *
   * @return a DefaultResponseErrorHandler instance
   */
  @Bean
  public ResponseErrorHandler defaultResponseErrorHandler() {
    return new DefaultResponseErrorHandler();
  }

  /**
   * Creates an ObservationRegistry bean for monitoring and observability.
   * This registry is used to track metrics and traces for AI operations,
   * enabling monitoring of performance and usage patterns.
   *
   * @return a new ObservationRegistry instance
   */
  @Bean
  public ObservationRegistry observationRegistry() {
    return ObservationRegistry.create();
  }

  /**
   * Creates an EmbeddingModelFactory bean responsible for creating embedding models
   * for different AI providers. This factory supports multiple providers including
   * OpenAI, Azure OpenAI, Ollama, and others.
   *
   * @param properties           AI Redis OM configuration properties
   * @param springAiProperties   Spring AI configuration properties
   * @param restClientBuilder    builder for creating REST clients
   * @param webClientBuilder     builder for creating reactive web clients
   * @param responseErrorHandler handler for HTTP response errors
   * @param observationRegistry  registry for monitoring and observability
   * @return a configured EmbeddingModelFactory instance
   */
  @Bean
  public EmbeddingModelFactory embeddingModelFactory(AIRedisOMProperties properties,
      SpringAiProperties springAiProperties, RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder,
      ResponseErrorHandler responseErrorHandler, ObservationRegistry observationRegistry) {
    return new EmbeddingModelFactory(properties, springAiProperties, restClientBuilder, webClientBuilder,
        responseErrorHandler, observationRegistry);
  }

  /**
   * Creates a DJL ImageFactory bean for image processing operations.
   * This factory is used to create and manipulate images for computer vision tasks
   * such as face detection and image embeddings.
   *
   * @return the singleton ImageFactory instance
   */
  @Bean(
      name = "djlImageFactory"
  )
  public ImageFactory imageFactory() {
    return ImageFactory.getInstance();
  }

  /**
   * Creates criteria for loading DJL image embedding models.
   * This criteria defines the model type, engine, and URLs for downloading
   * the image embedding model used to convert images into vector representations.
   *
   * @param properties AI Redis OM configuration properties containing DJL settings
   * @return criteria for loading image embedding models
   */
  @Bean(
      name = "djlImageEmbeddingModelCriteria"
  )
  public Criteria<Image, float[]> imageEmbeddingModelCriteria(AIRedisOMProperties properties) {
    return Criteria.builder().setTypes(Image.class, float[].class) //
        .optEngine(properties.getDjl().getImageEmbeddingModelEngine())  //
        .optModelUrls(properties.getDjl().getImageEmbeddingModelModelUrls()) //
        .build();
  }

  /**
   * Creates a translator for face detection models.
   * This translator handles the preprocessing and postprocessing of images
   * for face detection, including confidence thresholding and non-maximum suppression.
   *
   * @return a configured FaceDetectionTranslator with predefined detection parameters
   */
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

  /**
   * Creates criteria for loading DJL face detection models.
   * This criteria defines the model specifications including the translator,
   * engine, and URLs for the face detection model.
   *
   * @param translator the face detection translator for preprocessing/postprocessing
   * @param properties AI Redis OM configuration properties containing DJL settings
   * @return criteria for loading face detection models
   */
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

  /**
   * Loads the DJL face detection model based on the provided criteria.
   * This model is used to detect faces in images before extracting face embeddings.
   * If the model cannot be loaded, returns null and logs a warning.
   *
   * @param criteria the criteria for loading the face detection model (nullable)
   * @return the loaded face detection model, or null if loading fails
   */
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

  /**
   * Creates a translator for face embedding models.
   * This translator handles the preprocessing and postprocessing of face images
   * to extract facial feature vectors for similarity search.
   *
   * @return a new FaceFeatureTranslator instance
   */
  @Bean(
      name = "djlFaceEmbeddingTranslator"
  )
  public Translator<Image, float[]> faceEmbeddingTranslator() {
    return new FaceFeatureTranslator();
  }

  /**
   * Creates criteria for loading DJL face embedding models.
   * This criteria defines the model specifications for extracting facial features
   * as vector embeddings that can be used for face recognition and similarity search.
   *
   * @param translator the face embedding translator for preprocessing/postprocessing
   * @param properties AI Redis OM configuration properties containing DJL settings
   * @return criteria for loading face embedding models
   */
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

  /**
   * Loads the DJL face embedding model based on the provided criteria.
   * This model extracts facial features as vector embeddings for face recognition
   * and similarity search. If the model cannot be loaded, returns null and logs a warning.
   *
   * @param criteria the criteria for loading the face embedding model (nullable)
   * @return the loaded face embedding model, or null if loading fails
   */
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

  /**
   * Loads the DJL image embedding model based on the provided criteria.
   * This model converts general images into vector embeddings for similarity search
   * and image retrieval tasks.
   *
   * @param criteria the criteria for loading the image embedding model (nullable)
   * @return the loaded image embedding model, or null if criteria is null
   * @throws MalformedModelException if the model format is invalid
   * @throws ModelNotFoundException  if the model cannot be found
   * @throws IOException             if there's an error loading the model
   */
  @Bean(
      name = "djlImageEmbeddingModel"
  )
  public ZooModel<Image, float[]> imageModel(@Nullable @Qualifier(
    "djlImageEmbeddingModelCriteria"
  ) Criteria<Image, float[]> criteria) throws MalformedModelException, ModelNotFoundException, IOException {
    return criteria != null ? ModelZoo.loadModel(criteria) : null;
  }

  /**
   * Creates a default image preprocessing pipeline for DJL models.
   * This pipeline applies transformations such as center cropping, resizing,
   * and tensor conversion to prepare images for embedding models.
   *
   * @param properties AI Redis OM configuration properties containing pipeline settings
   * @return a configured Pipeline with image transformations
   */
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

  /**
   * Creates a HuggingFace sentence tokenizer for text processing.
   * This tokenizer is used to prepare text for sentence embedding models.
   * First attempts to load from bundled resources, then falls back to downloading
   * from HuggingFace if network is available.
   *
   * @param properties AI Redis OM configuration properties containing tokenizer settings
   * @return a configured HuggingFaceTokenizer, or null if unable to load
   */
  @Bean(
      name = "djlSentenceTokenizer"
  )
  public HuggingFaceTokenizer sentenceTokenizer(AIRedisOMProperties properties) {
    Map<String, String> options = Map.of( //
        "maxLength", properties.getDjl().getSentenceTokenizerMaxLength(), //
        "modelMaxLength", properties.getDjl().getSentenceTokenizerModelMaxLength() //
    );

    // First try to load from bundled resources (for CI/offline environments)
    try {
      String resourcePath = "/tokenizers/" + properties.getDjl().getSentenceTokenizerModel() + "/tokenizer.json";
      var resourceStream = getClass().getResourceAsStream(resourcePath);
      if (resourceStream != null) {
        logger.info("Loading HuggingFace tokenizer from bundled resources: " + resourcePath);
        return HuggingFaceTokenizer.newInstance(resourceStream, options);
      }
    } catch (Exception e) {
      logger.debug("Failed to load tokenizer from bundled resources, will try downloading", e);
    }

    // Fall back to downloading from HuggingFace (for normal environments)
    try {
      //noinspection ResultOfMethodCallIgnored
      InetAddress.getByName("www.huggingface.co").isReachable(5000);
      logger.info("Loading HuggingFace tokenizer from remote: " + properties.getDjl().getSentenceTokenizerModel());
      return HuggingFaceTokenizer.newInstance(properties.getDjl().getSentenceTokenizerModel(), options);
    } catch (IOException ioe) {
      logger.warn("Unable to download HuggingFace tokenizer (network unavailable or restricted environment)");
      return null;
    }
  }

  /**
   * Creates the primary Embedder bean responsible for generating embeddings.
   * This embedder integrates various AI providers and models to create vector embeddings
   * from text, images, and faces. It serves as the central component for the @Vectorize
   * annotation functionality.
   *
   * @param imageEmbeddingModel   DJL model for general image embeddings (nullable)
   * @param faceEmbeddingModel    DJL model for face embeddings (nullable)
   * @param imageFactory          factory for creating and processing images (nullable)
   * @param defaultImagePipeline  preprocessing pipeline for images (nullable)
   * @param properties            AI Redis OM configuration properties
   * @param embeddingModelFactory factory for creating embedding models for various providers
   * @param ac                    Spring application context for accessing other beans
   * @return a configured DefaultEmbedder instance
   */
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