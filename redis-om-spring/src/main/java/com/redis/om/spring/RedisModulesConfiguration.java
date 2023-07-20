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
import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.GsonBuilder;
import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.Cuckoo;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.pds.BloomOperations;
import com.redis.om.spring.ops.pds.CuckooFilterOperations;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.serialization.gson.*;
import com.redis.om.spring.vectorize.DefaultFeatureExtractor;
import com.redis.om.spring.vectorize.FeatureExtractor;
import com.redis.om.spring.vectorize.NoopFeatureExtractor;
import com.redis.om.spring.vectorize.face.FaceDetectionTranslator;
import com.redis.om.spring.vectorize.face.FaceFeatureTranslator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import redis.clients.jedis.bloom.CFReserveParams;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.redis.om.spring.util.ObjectUtils.getBeanDefinitionsFor;
import static com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({RedisProperties.class, RedisOMProperties.class})
@EnableAspectJAutoProxy
@ComponentScan("com.redis.om.spring.bloom")
@ComponentScan("com.redis.om.spring.cuckoo")
@ComponentScan("com.redis.om.spring.autocomplete")
@ComponentScan("com.redis.om.spring.metamodel")
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

  @Bean(name = "redisTemplate")
  @Primary
  public RedisTemplate<?, ?> redisTemplate(JedisConnectionFactory connectionFactory) {
    RedisTemplate<?, ?> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setDefaultSerializer(new StringRedisSerializer());
    template.setConnectionFactory(connectionFactory);

    return template;
  }

  @Bean(name = "rediSearchIndexer")
  public RediSearchIndexer redisearchIndexer(ApplicationContext ac) {
    return new RediSearchIndexer(ac);
  }

  @Bean(name = "djlImageFactory")
  public ImageFactory imageFactory() {
    return ImageFactory.getInstance();
  }

  @Bean(name = "djlImageEmbeddingModelCriteria")
  public Criteria<Image, byte[]> imageEmbeddingModelCriteria(RedisOMProperties properties) {
    return properties.getDjl().isEnabled() ? Criteria.builder()
        .setTypes(Image.class, byte[].class) //
        .optEngine(properties.getDjl().getImageEmbeddingModelEngine())  //
        .optModelUrls(properties.getDjl().getImageEmbeddingModelModelUrls()) //
        .build() : null;
  }

  @Bean(name = "djlFaceDetectionTranslator")
  public Translator<Image, DetectedObjects> faceDetectionTranslator() {
    double confThresh = 0.85f;
    double nmsThresh = 0.45f;
    double[] variance = {0.1f, 0.2f};
    int topK = 5000;
    int[][] scales = {{16, 32}, {64, 128}, {256, 512}};
    int[] steps = {8, 16, 32};
    return new FaceDetectionTranslator(confThresh, nmsThresh, variance, topK, scales, steps);
  }

  @Bean(name = "djlFaceDetectionModelCriteria")
  public Criteria<Image, DetectedObjects> faceDetectionModelCriteria( //
      @Qualifier("djlFaceDetectionTranslator") Translator<Image, DetectedObjects> translator, //
      RedisOMProperties properties) {

    return properties.getDjl().isEnabled() ? Criteria.builder()
        .setTypes(Image.class, DetectedObjects.class) //
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
            .setTypes(Image.class, float[].class)
            .optModelUrls(properties.getDjl().getFaceEmbeddingModelModelUrls()) //
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
      @Nullable @Qualifier("djlImageEmbeddingModelCriteria") Criteria<Image, byte[]> criteria, RedisOMProperties properties)
      throws MalformedModelException, ModelNotFoundException, IOException {
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
    } else return null;
  }

  @Bean(name = "djlSentenceTokenizer")
  public HuggingFaceTokenizer sentenceTokenizer(RedisOMProperties properties) {
    if (properties.getDjl().isEnabled()) {
      Map<String, String> options = Map.of( //
          "maxLength", properties.getDjl().getSentenceTokenizerMaxLength(), //
          "modelMaxLength", properties.getDjl().getSentenceTokenizerModelMaxLength() //
      );

      try {
        InetAddress.getByName("www.huggingface.co").isReachable(5000);
        return HuggingFaceTokenizer.newInstance(properties.getDjl().getSentenceTokenizerModel(), options);
      } catch (IOException ioe) {
        logger.warn("Error retrieving default DJL sentence tokenizer");
        return null;
      }
    } else return null;
  }

  @Bean(name = "featureExtractor")
  public FeatureExtractor featureExtractor(
      @Nullable @Qualifier("djlImageEmbeddingModel") ZooModel<Image, byte[]> imageEmbeddingModel,
      @Nullable @Qualifier("djlFaceEmbeddingModel") ZooModel<Image, float[]> faceEmbeddingModel,
      @Nullable @Qualifier("djlImageFactory") ImageFactory imageFactory,
      @Nullable @Qualifier("djlDefaultImagePipeline") Pipeline defaultImagePipeline,
      @Nullable @Qualifier("djlSentenceTokenizer") HuggingFaceTokenizer sentenceTokenizer,
      RedisOMProperties properties,
      ApplicationContext ac) {
    return properties.getDjl().isEnabled() ? new DefaultFeatureExtractor( ac, imageEmbeddingModel, faceEmbeddingModel, imageFactory, defaultImagePipeline, sentenceTokenizer) : new NoopFeatureExtractor();
  }

  @Bean(name = "redisJSONKeyValueAdapter")
  RedisJSONKeyValueAdapter getRedisJSONKeyValueAdapter( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> redisModulesOperations, //
      RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      @Qualifier("omGsonBuilder") GsonBuilder gsonBuilder, //
      RedisOMProperties properties //
  ) {
    return new RedisJSONKeyValueAdapter(redisOps, redisModulesOperations, mappingContext, indexer, gsonBuilder, properties);
  }

  @Bean(name = "redisJSONKeyValueTemplate")
  public CustomRedisKeyValueTemplate getRedisJSONKeyValueTemplate( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> redisModulesOperations, //
      RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      @Qualifier("omGsonBuilder") GsonBuilder gsonBuilder, //
      RedisOMProperties properties //
  ) {
    return new CustomRedisKeyValueTemplate(
        new RedisJSONKeyValueAdapter(redisOps, redisModulesOperations, mappingContext, indexer, gsonBuilder, properties),
        mappingContext);
  }

  @Bean(name = "redisCustomKeyValueTemplate")
  public CustomRedisKeyValueTemplate getKeyValueTemplate( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> redisModulesOperations, //
      RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      RedisOMProperties properties, //
      @Nullable @Qualifier("featureExtractor") FeatureExtractor featureExtractor
  ) {
    return new CustomRedisKeyValueTemplate(
        new RedisEnhancedKeyValueAdapter(redisOps, redisModulesOperations, mappingContext, indexer, featureExtractor, properties), //
        mappingContext);
  }

  @Bean(name = "streamingQueryBuilder")
  EntityStream streamingQueryBuilder(
      RedisModulesOperations<?> redisModulesOperations,
      @Qualifier("omGsonBuilder") GsonBuilder gsonBuilder,
      RediSearchIndexer indexer
  ) {
    return new EntityStreamImpl(redisModulesOperations, gsonBuilder, indexer);
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
    @SuppressWarnings("unchecked")
    RedisModulesOperations<String> rmo = (RedisModulesOperations<String>) ac.getBean("redisModulesOperations");

    Set<BeanDefinition> beanDefs = getBeanDefinitionsFor(ac, Document.class, RedisHash.class);

    for (BeanDefinition beanDef : beanDefs) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        for (java.lang.reflect.Field field : getDeclaredFieldsTransitively(cl)) {
          if (field.isAnnotationPresent(Bloom.class)) {
            Bloom bloom = field.getAnnotation(Bloom.class);
            BloomOperations<String> ops = rmo.opsForBloom();
            String filterName = !ObjectUtils.isEmpty(bloom.name()) ? bloom.name()
                : String.format("bf:%s:%s", cl.getSimpleName(), field.getName());
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
    @SuppressWarnings("unchecked")
    RedisModulesOperations<String> rmo = (RedisModulesOperations<String>) ac.getBean("redisModulesOperations");

    Set<BeanDefinition> beanDefs = getBeanDefinitionsFor(ac, Document.class, RedisHash.class);

    for (BeanDefinition beanDef : beanDefs) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        for (java.lang.reflect.Field field : getDeclaredFieldsTransitively(cl)) {
          if (field.isAnnotationPresent(Cuckoo.class)) {
            Cuckoo cuckoo = field.getAnnotation(Cuckoo.class);
            CuckooFilterOperations<String> ops = rmo.opsForCuckoFilter();
            String filterName = !ObjectUtils.isEmpty(cuckoo.name()) ? cuckoo.name()
                : String.format("cf:%s:%s", cl.getSimpleName(), field.getName());
            CFReserveParams params = CFReserveParams.reserveParams()
                .bucketSize(cuckoo.bucketSize())
                .expansion(cuckoo.expansion())
                .maxIterations(cuckoo.maxIterations());
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
    GsonBuilder gsonBuilder = (GsonBuilder)ac.getBean("omGsonBuilder");
    GsonReferenceSerializerRegistrar registrar = new GsonReferenceSerializerRegistrar(gsonBuilder, ac);

    registrar.registerReferencesFor(Document.class);
    registrar.registerReferencesFor(RedisHash.class);
  }
}
