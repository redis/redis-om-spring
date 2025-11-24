package com.redis.romsmultiaclaccount.config;

import java.time.Duration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.thread.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.lang.Nullable;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.CustomRedisKeyValueTemplate;
import com.redis.om.spring.RedisJSONKeyValueAdapter;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.vectorize.Embedder;

@Configuration
@EnableConfigurationProperties(
  { DataRedisProperties.class }
)
public class RedisConnectionFactoryConfig {

  private static final Log logger = LogFactory.getLog(RedisConnectionFactoryConfig.class);

  private final DataRedisProperties redisProperties;

  public RedisConnectionFactoryConfig(DataRedisProperties redisProperties) {
    this.redisProperties = redisProperties;
  }

  @Bean(
      name = "writeJedisConnectionFactory"
  )
  @ConditionalOnThreading(
    Threading.PLATFORM
  )
  public JedisConnectionFactory writeJedisConnectionFactory() {
    return createJedisConnectionFactory("userA", "passwordA");
  }

  @Bean(
      name = "writeJedisConnectionFactoryVirtual"
  )
  @ConditionalOnThreading(
    Threading.VIRTUAL
  )
  public JedisConnectionFactory writeJedisConnectionFactoryVirtual() {
    JedisConnectionFactory factory = createJedisConnectionFactory("userA", "passwordA");
    SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("redis-write-");
    executor.setVirtualThreads(true);
    factory.setExecutor(executor);
    return factory;
  }

  @Primary
  @Bean(
      name = "readJedisConnectionFactory"
  )
  public JedisConnectionFactory readJedisConnectionFactory() {
    return createJedisConnectionFactory("userB", "passwordB");
  }

  private JedisConnectionFactory createJedisConnectionFactory(String username, String password) {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties
        .getPort());
    config.setDatabase(redisProperties.getDatabase());
    config.setUsername(username);
    config.setPassword(password);

    JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().connectTimeout(Duration.ofMillis(
        redisProperties.getTimeout().toMillis())).build();

    return new JedisConnectionFactory(config, clientConfig);
  }

  @Bean(
      name = "writeRedisModulesClient"
  )
  public RedisModulesClient writeRedisModulesClient(@Qualifier(
    "writeJedisConnectionFactory"
  ) JedisConnectionFactory factory, @Qualifier(
    "omGsonBuilder"
  ) GsonBuilder builder) {
    return new RedisModulesClient(factory, builder);
  }

  @Bean(
      name = "readRedisModulesClient"
  )
  public RedisModulesClient readRedisModulesClient(@Qualifier(
    "readJedisConnectionFactory"
  ) JedisConnectionFactory factory, @Qualifier(
    "omGsonBuilder"
  ) GsonBuilder builder) {
    return new RedisModulesClient(factory, builder);
  }

  @Bean(
      name = "writeRedisModulesOperations"
  )
  public RedisModulesOperations<?> writeRedisModulesOperations(@Qualifier(
    "writeRedisModulesClient"
  ) RedisModulesClient client, @Qualifier(
    "writeJedisConnectionFactory"
  ) JedisConnectionFactory factory, @Qualifier(
    "omGsonBuilder"
  ) GsonBuilder builder) {
    return new RedisModulesOperations<>(client, new StringRedisTemplate(factory), builder);
  }

  @Bean(
      name = "readRedisModulesOperations"
  )
  public RedisModulesOperations<?> readRedisModulesOperations(@Qualifier(
    "readRedisModulesClient"
  ) RedisModulesClient client, @Qualifier(
    "readJedisConnectionFactory"
  ) JedisConnectionFactory factory, @Qualifier(
    "omGsonBuilder"
  ) GsonBuilder builder) {
    return new RedisModulesOperations<>(client, new StringRedisTemplate(factory), builder);
  }

  @Bean(
      name = "writeRedisOperations"
  )
  public RedisOperations<String, String> writeRedisOperations(@Qualifier(
    "writeJedisConnectionFactory"
  ) JedisConnectionFactory factory) {
    return new StringRedisTemplate(factory);
  }

  @Bean(
      name = "readRedisOperations"
  )
  public RedisOperations<String, String> readRedisOperations(@Qualifier(
    "readJedisConnectionFactory"
  ) JedisConnectionFactory factory) {
    return new StringRedisTemplate(factory);
  }

  @Bean(
      name = "writeKeyValueTemplate"
  )
  public CustomRedisKeyValueTemplate getWriteRedisJSONKeyValueTemplate(@Qualifier(
    "writeRedisOperations"
  ) RedisOperations<?, ?> redisOps, @Qualifier(
    "writeRedisModulesOperations"
  ) RedisModulesOperations<?> redisModulesOperations, @Qualifier(
    "redisEnhancedMappingContext"
  ) RedisMappingContext mappingContext, RediSearchIndexer indexer, @Qualifier(
    "omGsonBuilder"
  ) GsonBuilder gsonBuilder, RedisOMProperties properties, @Nullable @Qualifier(
    "featureExtractor"
  ) Embedder embedder) {
    return new CustomRedisKeyValueTemplate(new RedisJSONKeyValueAdapter(redisOps, redisModulesOperations,
        mappingContext, indexer, gsonBuilder, embedder, properties), mappingContext);
  }

  @Bean(
      name = "readKeyValueTemplate"
  )
  public CustomRedisKeyValueTemplate getReadRedisJSONKeyValueTemplate(@Qualifier(
    "readRedisOperations"
  ) RedisOperations<?, ?> redisOps, @Qualifier(
    "readRedisModulesOperations"
  ) RedisModulesOperations<?> redisModulesOperations, @Qualifier(
    "redisEnhancedMappingContext"
  ) RedisMappingContext mappingContext, RediSearchIndexer indexer, @Qualifier(
    "omGsonBuilder"
  ) GsonBuilder gsonBuilder, RedisOMProperties properties, @Nullable @Qualifier(
    "featureExtractor"
  ) Embedder embedder) {
    return new CustomRedisKeyValueTemplate(new RedisJSONKeyValueAdapter(redisOps, redisModulesOperations,
        mappingContext, indexer, gsonBuilder, embedder, properties), mappingContext);
  }

  @Primary
  @Bean(
      name = "redisModulesOperations"
  )
  public RedisModulesOperations<?> redisModulesOperationsAlias(@Qualifier(
    "readRedisModulesOperations"
  ) RedisModulesOperations<?> readOps) {
    return readOps;
  }
}