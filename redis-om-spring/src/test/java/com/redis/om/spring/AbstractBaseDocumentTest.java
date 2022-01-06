package com.redis.om.spring;

import static com.redis.testcontainers.RedisClusterContainer.DEFAULT_IMAGE_NAME;

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

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.testcontainers.RedisModulesContainer;

@Testcontainers
@SpringBootTest(classes = AbstractBaseDocumentTest.Config.class, properties = {"spring.main.allow-bean-definition-overriding=true"})
public abstract class AbstractBaseDocumentTest {
  @Container
  static final RedisModulesContainer REDIS = new RedisModulesContainer(DEFAULT_IMAGE_NAME.withTag("edge"));
  
  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(basePackages = "com.redis.om.spring.annotations.document.fixtures")
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
