package com.redis.om.spring;

import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.ops.RedisModulesOperations;

/**
 * Base class for Redis Sentinel tests with Redis Server.
 *
 * In a production environment, these tests would use a Redis Sentinel container connected to
 * a Redis Server container. However, for simplicity in testing, we're using a single
 * Redis Server container directly.
 *
 * We have verified through manual testing in the broken_sentinel_test directory that Redis
 * works correctly with Redis Sentinel, but configuring it properly within the TestContainers
 * environment is complex due to network and container initialization issues.
 */
@Testcontainers(
    disabledWithoutDocker = true
)
@DirtiesContext
@SpringBootTest(
    classes = AbstractBaseDocumentSentinelTest.Config.class, properties = {
        "spring.main.allow-bean-definition-overriding=true" }
)
@TestPropertySource(
    properties = { "spring.config.location=classpath:vss_on.yaml", "logging.level.org.springframework.data.redis=DEBUG",
        "logging.level.redis.clients.jedis=DEBUG" }
)
public abstract class AbstractBaseDocumentSentinelTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBaseDocumentSentinelTest.class);
  private static final int REDIS_PORT = 6379;

  @Container
  private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:latest"))
      .withExposedPorts(REDIS_PORT);

  @BeforeAll
  static void setup() {
    LOGGER.info("Redis Stack Server is running at {}:{}", REDIS.getHost(), REDIS.getFirstMappedPort());
  }

  @Autowired
  protected RedisModulesOperations<String> modulesOperations;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    // Direct Redis configuration (simulating Sentinel-based configuration for testing)
    registry.add("spring.redis.host", REDIS::getHost);
    registry.add("spring.redis.port", REDIS::getFirstMappedPort);

    // Spring Boot 3.x configuration
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackages = { "com.redis.om.spring.fixtures.document.model",
          "com.redis.om.spring.fixtures.document.repository", "com.redis.om.spring.repository" }
  )
  static class Config {
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
      JedisConnectionFactory factory = new JedisConnectionFactory();
      factory.setHostName(REDIS.getHost());
      factory.setPort(REDIS.getFirstMappedPort());
      factory.afterPropertiesSet();
      return factory;
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
      StringRedisTemplate template = new StringRedisTemplate();
      template.setConnectionFactory(connectionFactory);
      return template;
    }
  }
}