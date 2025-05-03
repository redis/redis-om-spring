package com.redis.om.spring;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.GsonBuilder;
import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.Cuckoo;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.mapping.RedisEnhancedMappingContext;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.pds.BloomOperations;
import com.redis.om.spring.ops.pds.CountMinSketchOperations;
import com.redis.om.spring.ops.pds.CuckooFilterOperations;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.serialization.gson.*;
import com.redis.om.spring.vectorize.Embedder;
import com.redis.om.spring.vectorize.NoopEmbedder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import redis.clients.jedis.bloom.CFReserveParams;

import java.time.*;
import java.util.Date;
import java.util.List;
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

  @Bean(name = "redisEnhancedMappingContext")
  @Primary
  public RedisEnhancedMappingContext redisMappingContext() {
    return new RedisEnhancedMappingContext();
  }

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

  @Bean(name = "redisCountminOperations")
  CountMinSketchOperations<?> redisCountMinOperations(RedisModulesOperations<?> redisModulesOperations) {
    return redisModulesOperations.opsForCountMinSketch();
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

  @Bean(name = "redisJSONKeyValueAdapter")
  RedisJSONKeyValueAdapter getRedisJSONKeyValueAdapter( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> redisModulesOperations, //
      @Qualifier("redisEnhancedMappingContext") RedisMappingContext mappingContext, //
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
      @Qualifier("redisEnhancedMappingContext") RedisMappingContext mappingContext, //
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
      @Qualifier("redisEnhancedMappingContext") RedisMappingContext mappingContext, //
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

  @ConditionalOnProperty(name = "redis.om.spring.ai.enabled", havingValue = "false", matchIfMissing = true)
  @Bean(name = "featureExtractor")
  public Embedder featureExtractor() {
    return new NoopEmbedder();
  }
}
