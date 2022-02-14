package com.redis.om.spring;

import static com.redis.testcontainers.RedisModulesContainer.DEFAULT_IMAGE_NAME;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.testcontainers.RedisModulesContainer;

import redis.clients.jedis.JedisPoolConfig;

@Testcontainers
@DirtiesContext
@SpringBootTest(classes = AbstractBaseDocumentTest.Config.class, properties = {"spring.main.allow-bean-definition-overriding=true"})
public abstract class AbstractBaseDocumentTest {
  @Container
  static final RedisModulesContainer REDIS;
  
  static {
    REDIS = new RedisModulesContainer(DEFAULT_IMAGE_NAME.withTag("edge")).withReuse(true);
    REDIS.start();
  }
  
  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
      registry.add("spring.redis.host", REDIS::getContainerIpAddress);
      registry.add("spring.redis.port", REDIS::getFirstMappedPort);
  }
  
  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(basePackages = "com.redis.om.spring.annotations.document.fixtures")
  static class Config {
    @Autowired
    Environment env;
    
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
      String host = env.getProperty("spring.redis.host");
      int port = env.getProperty("spring.redis.port", Integer.class);
      
      RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration(host, port);
      
      final JedisPoolConfig poolConfig = new JedisPoolConfig();
      poolConfig.setTestWhileIdle(true);
      poolConfig.setMinEvictableIdleTime(Duration.ofMillis(60000));
      poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
      poolConfig.setNumTestsPerEvictionRun(-1);
      
      final Integer timeout = 10000;
      
      final JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder()
          .connectTimeout(Duration.ofMillis(timeout))
          .readTimeout(Duration.ofMillis(timeout))
          .usePooling()
          .poolConfig(poolConfig)
          .build();

      return new JedisConnectionFactory(conf, jedisClientConfiguration);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
      RedisTemplate<?, ?> template = new RedisTemplate<>();
      template.setConnectionFactory(connectionFactory);

      return template;
    }
    
    
  }
}
