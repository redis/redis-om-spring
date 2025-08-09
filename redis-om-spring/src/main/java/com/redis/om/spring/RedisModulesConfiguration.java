package com.redis.om.spring;

import static com.redis.om.spring.util.ObjectUtils.getBeanDefinitionsFor;
import static com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

import redis.clients.jedis.bloom.CFReserveParams;

/**
 * Main configuration class for Redis OM Spring framework.
 * <p>
 * This class provides the core Spring configuration for Redis OM Spring, including:
 * <ul>
 * <li>Redis modules operations and templates for JSON and hash-based entities</li>
 * <li>Search indexing capabilities using RediSearch</li>
 * <li>Enhanced mapping contexts for Redis entity metadata</li>
 * <li>Probabilistic data structure support (Bloom filters, Cuckoo filters, etc.)</li>
 * <li>Auto-completion and suggestion features</li>
 * <li>Event-driven index creation and probabilistic data structure initialization</li>
 * </ul>
 * <p>
 * The configuration automatically scans for Redis OM annotations and creates
 * the necessary Redis indexes and data structures when the application context
 * is refreshed.
 *
 * @since 1.0.0
 */
@Configuration(
    proxyBeanMethods = false
)
@EnableConfigurationProperties(
  { RedisProperties.class, RedisOMProperties.class }
)
@EnableAspectJAutoProxy
@ComponentScan(
  "com.redis.om.spring.bloom"
)
@ComponentScan(
  "com.redis.om.spring.cuckoo"
)
@ComponentScan(
  "com.redis.om.spring.autocomplete"
)
@ComponentScan(
  "com.redis.om.spring.metamodel"
)
@ComponentScan(
  "com.redis.om.spring.util"
)
public class RedisModulesConfiguration {

  /**
   * Default constructor for Redis modules configuration.
   * <p>
   * This constructor is used by Spring's dependency injection framework
   * to create the configuration instance.
   */
  public RedisModulesConfiguration() {
    // Default constructor for Spring instantiation
  }

  private static final Log logger = LogFactory.getLog(RedisModulesConfiguration.class);

  /**
   * Creates the default Redis mapping context for enhanced entity mapping.
   * <p>
   * This mapping context provides metadata about Redis-mapped entities including
   * field information, type conversions, and persistence properties.
   * <p>
   * Users can override this by providing their own bean named "redisEnhancedMappingContext"
   * with @Primary annotation. The @ConditionalOnMissingBean ensures this default
   * bean is only created if users haven't provided their own.
   *
   * @return the enhanced mapping context instance
   */
  @Bean(
      name = "redisEnhancedMappingContext"
  )
  @Primary
  @ConditionalOnMissingBean(
      name = "redisEnhancedMappingContext"
  )
  public RedisEnhancedMappingContext redisMappingContext() {
    return new RedisEnhancedMappingContext();
  }

  /**
   * Creates a configured Gson builder for JSON serialization and deserialization.
   * <p>
   * This builder is customized with any available {@link GsonBuilderCustomizer} beans
   * and includes built-in Redis OM Spring type adapters for proper serialization
   * of Redis-specific data types.
   *
   * @param customizers list of customizers to apply to the builder
   * @return the configured Gson builder instance
   */
  @Bean(
      name = "omGsonBuilder"
  )
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

    // Register factory for handling Boolean values in Maps (must be after type adapters)
    builder.registerTypeAdapterFactory(MapBooleanTypeAdapterFactory.getInstance());

    return builder;
  }

  /**
   * Creates the Redis modules client for accessing Redis Stack modules.
   * <p>
   * This client provides low-level access to Redis modules including RedisJSON,
   * RediSearch, RedisBloom, and other Redis Stack capabilities. It serves as
   * the foundation for higher-level operations beans.
   *
   * @param jedisConnectionFactory the Jedis connection factory for Redis connectivity
   * @param builder                the Gson builder for JSON serialization
   * @return the Redis modules client instance
   */
  @Bean(
      name = "redisModulesClient"
  )
  @Lazy
  RedisModulesClient redisModulesClient( //
      JedisConnectionFactory jedisConnectionFactory, //
      @Qualifier(
        "omGsonBuilder"
      ) GsonBuilder builder) {
    return new RedisModulesClient(jedisConnectionFactory, builder);
  }

  /**
   * Creates the primary Redis modules operations bean for high-level module commands.
   * <p>
   * This bean provides a unified interface for accessing all Redis modules operations,
   * including JSON, Search, Bloom filters, and other probabilistic data structures.
   * It serves as the central operations hub for Redis OM Spring functionality.
   *
   * @param rmc         the Redis modules client for low-level access
   * @param template    the string Redis template for basic operations
   * @param gsonBuilder the Gson builder for JSON serialization
   * @return the Redis modules operations instance
   */
  @Bean(
      name = "redisModulesOperations"
  )
  @Primary
  @ConditionalOnMissingBean
  @Lazy
  RedisModulesOperations<?> redisModulesOperations( //
      RedisModulesClient rmc, //
      StringRedisTemplate template, //
      @Qualifier(
        "omGsonBuilder"
      ) GsonBuilder gsonBuilder) {
    return new RedisModulesOperations<>(rmc, template, gsonBuilder);
  }

  /**
   * Creates the JSON operations bean for RedisJSON commands.
   * <p>
   * This bean provides operations for manipulating JSON documents stored in Redis,
   * including get, set, del, and path-based operations.
   *
   * @param redisModulesOperations the Redis modules operations instance
   * @return the JSON operations instance
   */
  @Bean(
      name = "redisJSONOperations"
  )
  JSONOperations<?> redisJSONOperations(RedisModulesOperations<?> redisModulesOperations) {
    return redisModulesOperations.opsForJSON();
  }

  /**
   * Creates the Bloom filter operations bean for probabilistic data structures.
   * <p>
   * This bean provides operations for Bloom filters, including creation,
   * addition of items, and membership testing.
   *
   * @param redisModulesOperations the Redis modules operations instance
   * @return the Bloom filter operations instance
   */
  @Bean(
      name = "redisBloomOperations"
  )
  BloomOperations<?> redisBloomOperations(RedisModulesOperations<?> redisModulesOperations) {
    return redisModulesOperations.opsForBloom();
  }

  /**
   * Creates the Cuckoo filter operations bean for probabilistic data structures.
   * <p>
   * This bean provides operations for Cuckoo filters, which offer better space
   * efficiency than Bloom filters and support deletions.
   *
   * @param redisModulesOperations the Redis modules operations instance
   * @return the Cuckoo filter operations instance
   */
  @Bean(
      name = "redisCuckooOperations"
  )
  CuckooFilterOperations<?> redisCuckooFilterOperations(RedisModulesOperations<?> redisModulesOperations) {
    return redisModulesOperations.opsForCuckoFilter();
  }

  /**
   * Creates the Count-Min Sketch operations bean for probabilistic counting.
   * <p>
   * This bean provides operations for Count-Min Sketch data structures,
   * which allow approximate frequency counting with bounded error.
   *
   * @param redisModulesOperations the Redis modules operations instance
   * @return the Count-Min Sketch operations instance
   */
  @Bean(
      name = "redisCountminOperations"
  )
  CountMinSketchOperations<?> redisCountMinOperations(RedisModulesOperations<?> redisModulesOperations) {
    return redisModulesOperations.opsForCountMinSketch();
  }

  /**
   * Creates the primary Redis template for general Redis operations.
   * <p>
   * This template is configured with string serializers for both keys and values,
   * providing a consistent serialization strategy across the Redis OM Spring framework.
   * It serves as the foundation for Redis operations that don't require module-specific
   * functionality.
   *
   * @param connectionFactory the Jedis connection factory for Redis connectivity
   * @return the configured Redis template instance
   */
  @Bean(
      name = "redisOmTemplate"
  )
  @Primary
  public RedisTemplate<?, ?> redisTemplate(JedisConnectionFactory connectionFactory) {
    RedisTemplate<?, ?> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setDefaultSerializer(new StringRedisSerializer());
    template.setConnectionFactory(connectionFactory);

    return template;
  }

  /**
   * Creates the RediSearch indexer for managing search indexes.
   * <p>
   * This indexer is responsible for creating, updating, and managing RediSearch
   * indexes for entities annotated with {@code @Document} and {@code @RedisHash}.
   * It scans entity classes for indexable fields and creates the appropriate
   * search indexes in Redis.
   *
   * @param ac          the application context for bean discovery
   * @param properties  the Redis OM configuration properties
   * @param gsonBuilder the Gson builder for JSON serialization
   * @return the configured RediSearch indexer instance
   */
  @Bean(
      name = "rediSearchIndexer"
  )
  public RediSearchIndexer redisearchIndexer(ApplicationContext ac, //
      RedisOMProperties properties, //
      @Qualifier(
        "omGsonBuilder"
      ) GsonBuilder gsonBuilder) {

    return new RediSearchIndexer(ac, properties, gsonBuilder);
  }

  /**
   * Creates the Redis JSON key-value adapter for JSON document persistence.
   * <p>
   * This adapter handles the mapping between JSON documents stored in Redis
   * and Java entity objects, providing seamless persistence operations for
   * document-based data models.
   *
   * @param redisOps               the Redis operations template
   * @param redisModulesOperations the Redis modules operations for JSON
   * @param mappingContext         the Redis mapping context
   * @param indexer                the search indexer for creating indexes
   * @param gsonBuilder            the Gson builder for JSON serialization
   * @param properties             the Redis OM configuration properties
   * @param embedder               optional embedder for vector generation
   * @return the configured JSON key-value adapter
   */
  @Bean(
      name = "redisJSONKeyValueAdapter"
  )
  RedisJSONKeyValueAdapter getRedisJSONKeyValueAdapter( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> redisModulesOperations, //
      @Qualifier(
        "redisEnhancedMappingContext"
      ) RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      @Qualifier(
        "omGsonBuilder"
      ) GsonBuilder gsonBuilder, //
      RedisOMProperties properties, //
      @Nullable @Qualifier(
        "featureExtractor"
      ) Embedder embedder) {
    return new RedisJSONKeyValueAdapter(redisOps, redisModulesOperations, mappingContext, indexer, gsonBuilder,
        embedder, properties);
  }

  /**
   * Creates the Redis JSON key-value template for document operations.
   * <p>
   * This template provides higher-level operations for JSON document persistence,
   * building on the JSON key-value adapter to offer repository-style functionality
   * for document-based entities.
   *
   * @param redisOps               the Redis operations template
   * @param redisModulesOperations the Redis modules operations for JSON
   * @param mappingContext         the Redis mapping context
   * @param indexer                the search indexer for creating indexes
   * @param gsonBuilder            the Gson builder for JSON serialization
   * @param properties             the Redis OM configuration properties
   * @param embedder               optional embedder for vector generation
   * @return the configured JSON key-value template
   */
  @Bean(
      name = "redisJSONKeyValueTemplate"
  )
  public CustomRedisKeyValueTemplate getRedisJSONKeyValueTemplate( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> redisModulesOperations, //
      @Qualifier(
        "redisEnhancedMappingContext"
      ) RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      @Qualifier(
        "omGsonBuilder"
      ) GsonBuilder gsonBuilder, //
      RedisOMProperties properties, //
      @Nullable @Qualifier(
        "featureExtractor"
      ) Embedder embedder) {
    return new CustomRedisKeyValueTemplate(new RedisJSONKeyValueAdapter(redisOps, redisModulesOperations,
        mappingContext, indexer, gsonBuilder, embedder, properties), mappingContext);
  }

  /**
   * Creates the Redis enhanced key-value template for hash-based operations.
   * <p>
   * This template provides enhanced repository functionality for entities stored
   * as Redis hashes, supporting field-level operations and RediSearch capabilities
   * for hash-based data models.
   *
   * @param redisOps               the Redis operations template
   * @param redisModulesOperations the Redis modules operations for enhanced features
   * @param mappingContext         the Redis mapping context
   * @param indexer                the search indexer for creating indexes
   * @param properties             the Redis OM configuration properties
   * @param embedder               optional embedder for vector generation
   * @return the configured enhanced key-value template
   */
  @Bean(
      name = "redisCustomKeyValueTemplate"
  )
  public CustomRedisKeyValueTemplate getKeyValueTemplate( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> redisModulesOperations, //
      @Qualifier(
        "redisEnhancedMappingContext"
      ) RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      RedisOMProperties properties, //
      @Nullable @Qualifier(
        "featureExtractor"
      ) Embedder embedder) {
    return new CustomRedisKeyValueTemplate(new RedisEnhancedKeyValueAdapter(redisOps, redisModulesOperations,
        mappingContext, indexer, embedder, properties), //
        mappingContext);
  }

  /**
   * Creates the entity stream for fluent query building.
   * <p>
   * This bean provides a fluent API for building and executing complex queries
   * against Redis entities using a stream-like interface. It supports filtering,
   * sorting, aggregation, and other query operations in a type-safe manner.
   *
   * @param redisModulesOperations the Redis modules operations for query execution
   * @param gsonBuilder            the Gson builder for JSON serialization
   * @param indexer                the search indexer for metadata access
   * @return the entity stream instance for fluent queries
   */
  @Bean(
      name = "streamingQueryBuilder"
  )
  EntityStream streamingQueryBuilder(RedisModulesOperations<?> redisModulesOperations, @Qualifier(
    "omGsonBuilder"
  ) GsonBuilder gsonBuilder, RediSearchIndexer indexer) {
    return new EntityStreamImpl(redisModulesOperations, gsonBuilder, indexer);
  }

  /**
   * Creates a cache manager for Redis OM Spring internal caching.
   * <p>
   * This cache manager is used internally by Redis OM Spring for caching
   * metadata, search results, and other frequently accessed data to improve
   * performance. It uses a concurrent map-based implementation suitable
   * for development and testing environments.
   *
   * @return the cache manager instance for internal caching
   */
  @Bean(
      name = "redisOMCacheManager"
  )
  public CacheManager getCacheManager() {
    return new ConcurrentMapCacheManager();
  }

  /**
   * Ensures that all required search indexes are created when the application context is refreshed.
   * <p>
   * This event listener is triggered when the Spring application context is fully initialized,
   * scanning for entities annotated with {@code @Document} and {@code @RedisHash} and creating
   * the corresponding RediSearch indexes. This guarantees that all required indexes exist
   * before the application starts processing requests.
   *
   * @param cre the context refreshed event containing the application context
   */
  @EventListener(
    ContextRefreshedEvent.class
  )
  public void ensureIndexesAreCreated(ContextRefreshedEvent cre) {
    logger.info("Creating Indexes......");

    ApplicationContext ac = cre.getApplicationContext();

    RediSearchIndexer indexer = (RediSearchIndexer) ac.getBean("rediSearchIndexer");
    indexer.createIndicesFor(Document.class);
    indexer.createIndicesFor(RedisHash.class);
  }

  /**
   * Processes Bloom filter annotations and creates corresponding filters in Redis.
   * <p>
   * This event listener is triggered when the application context is refreshed,
   * scanning for fields annotated with {@code @Bloom} and creating the corresponding
   * Bloom filters in Redis with the specified capacity and error rate.
   *
   * @param cre the context refreshed event containing the application context
   */
  @EventListener(
    ContextRefreshedEvent.class
  )
  public void processBloom(ContextRefreshedEvent cre) {
    ApplicationContext ac = cre.getApplicationContext();
    @SuppressWarnings(
      "unchecked"
    ) RedisModulesOperations<String> rmo = (RedisModulesOperations<String>) ac.getBean("redisModulesOperations");

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

  /**
   * Processes Cuckoo filter annotations and creates corresponding filters in Redis.
   * <p>
   * This event listener is triggered when the application context is refreshed,
   * scanning for fields annotated with {@code @Cuckoo} and creating the corresponding
   * Cuckoo filters in Redis with the specified capacity and configuration parameters.
   * Cuckoo filters provide space-efficient approximate membership testing with
   * support for deletions.
   *
   * @param cre the context refreshed event containing the application context
   */
  @EventListener(
    ContextRefreshedEvent.class
  )
  public void processCuckoo(ContextRefreshedEvent cre) {
    ApplicationContext ac = cre.getApplicationContext();
    @SuppressWarnings(
      "unchecked"
    ) RedisModulesOperations<String> rmo = (RedisModulesOperations<String>) ac.getBean("redisModulesOperations");

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
            CFReserveParams params = CFReserveParams.reserveParams().bucketSize(cuckoo.bucketSize()).expansion(cuckoo
                .expansion()).maxIterations(cuckoo.maxIterations());
            ops.createFilter(filterName, cuckoo.capacity(), params);
          }
        }
      } catch (Exception e) {
        logger.debug("Error during processing of @Cuckoo annotation: ", e);
      }
    }
  }

  /**
   * Registers reference serializers for entity relationships during context initialization.
   * <p>
   * This event listener is triggered when the application context is refreshed,
   * registering custom Gson serializers for handling entity references and relationships.
   * This ensures that entity references are properly serialized and deserialized
   * when storing and retrieving complex object graphs in Redis.
   *
   * @param cre the context refreshed event containing the application context
   */
  @EventListener(
    ContextRefreshedEvent.class
  )
  public void registerReferenceSerializer(ContextRefreshedEvent cre) {
    logger.info("Registering Reference Serializers......");

    ApplicationContext ac = cre.getApplicationContext();
    GsonBuilder gsonBuilder = (GsonBuilder) ac.getBean("omGsonBuilder");
    GsonReferenceSerializerRegistrar registrar = new GsonReferenceSerializerRegistrar(gsonBuilder, ac);

    registrar.registerReferencesFor(Document.class);
    registrar.registerReferencesFor(RedisHash.class);
  }

  /**
   * Creates a no-operation embedder when AI features are disabled.
   * <p>
   * This bean provides a default embedder implementation that performs no operations
   * when AI features are explicitly disabled or when the AI module is not available.
   * This ensures that the application can function normally without AI capabilities
   * while maintaining the same interface contracts.
   *
   * @return a no-operation embedder instance
   */
  @ConditionalOnProperty(
      name = "redis.om.spring.ai.enabled", havingValue = "false", matchIfMissing = true
  )
  @Bean(
      name = "featureExtractor"
  )
  public Embedder featureExtractor() {
    return new NoopEmbedder();
  }
}
