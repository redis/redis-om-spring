package com.redis.om.spring;

import static org.springframework.util.StringUtils.commaDelimitedListToSet;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import redis.clients.jedis.JedisPoolConfig;

/**
 * Configuration class for Redis Sentinel setup.
 * <p>
 * This configuration class provides beans for connecting to Redis using Redis Sentinel
 * for high availability. It configures the Jedis connection factory with sentinel
 * configuration and provides a string Redis template for operations.
 * </p>
 * <p>
 * The configuration reads sentinel master and nodes from environment properties:
 * </p>
 * <ul>
 * <li>{@code spring.redis.sentinel.master} - The sentinel master name</li>
 * <li>{@code spring.redis.sentinel.nodes} - Comma-delimited list of sentinel nodes</li>
 * </ul>
 */
public class SentinelConfig {

  /**
   * Default constructor for Sentinel configuration.
   * <p>
   * This constructor is used by Spring's dependency injection framework
   * to create the configuration instance.
   */
  public SentinelConfig() {
    // Default constructor for Spring instantiation
  }

  /**
   * Creates a Jedis connection factory configured for Redis Sentinel.
   * <p>
   * This factory is configured with connection pooling, timeouts, and sentinel
   * configuration based on environment properties. The connection pool is optimized
   * for production use with appropriate eviction and testing settings.
   * </p>
   *
   * @param env the Spring environment for reading configuration properties
   * @return a configured Jedis connection factory for sentinel
   */
  @Bean
  public JedisConnectionFactory jedisConnectionFactory(Environment env) {
    String master = env.getProperty("spring.redis.sentinel.master", "localhost");
    String nodes = env.getProperty("spring.redis.sentinel.nodes");
    Set<String> sentinelNodes = commaDelimitedListToSet(nodes);
    Set<RedisNode> redisNodes = sentinelNodes.stream().map(RedisNode::fromString).collect(Collectors.toSet());

    RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration().master(master);
    sentinelConfig.setSentinels(redisNodes);

    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
    poolConfig.setNumTestsPerEvictionRun(-1);
    poolConfig.setTestWhileIdle(false);
    poolConfig.setTestOnReturn(false);
    poolConfig.setTestOnBorrow(false);

    final int timeout = 10000;

    final JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().connectTimeout(Duration
        .ofMillis(timeout)).readTimeout(Duration.ofMillis(timeout)).usePooling().poolConfig(poolConfig).build();

    return new JedisConnectionFactory(sentinelConfig, jedisClientConfiguration);
  }

  /**
   * Creates a string Redis template using the provided connection factory.
   * <p>
   * This template is configured to work with the sentinel connection factory
   * and provides string-based operations for Redis commands.
   * </p>
   *
   * @param connectionFactory the Redis connection factory (typically the sentinel factory)
   * @return a configured string Redis template
   */
  @Bean
  public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
    StringRedisTemplate template = new StringRedisTemplate();
    template.setConnectionFactory(connectionFactory);

    return template;
  }
}