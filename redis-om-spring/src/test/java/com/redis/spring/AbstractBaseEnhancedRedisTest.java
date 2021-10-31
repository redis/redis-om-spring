package com.redis.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.redis.spring.annotations.EnableRedisEnhancedRepositories;
import com.redis.testcontainers.RedisModulesContainer;

@Testcontainers
@SpringBootTest(classes = AbstractBaseEnhancedRedisTest.Config.class, properties = {"spring.main.allow-bean-definition-overriding=true"})
public abstract class AbstractBaseEnhancedRedisTest {
  @Container
  static final RedisModulesContainer REDIS = new RedisModulesContainer("edge");
  
  @SpringBootApplication
  @Configuration
  @EnableRedisEnhancedRepositories
  static class Config {
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
      RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration(REDIS.getContainerIpAddress(),
          REDIS.getMappedPort(6379));

      return new JedisConnectionFactory(conf);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
      RedisTemplate<?, ?> template = new RedisTemplate<>();
      template.setConnectionFactory(connectionFactory);

      return template;
    }
  }
}
