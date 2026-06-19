package com.redis.om.hashes;

import static com.redis.testcontainers.RedisStackContainer.DEFAULT_IMAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.redis.testcontainers.RedisStackContainer;

@SpringBootTest(
    classes = RomsHashesApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers(
    disabledWithoutDocker = true
)
class RomsHashesBootRunContextTest {

  @Container
  static final RedisStackContainer REDIS;

  static {
    REDIS = new RedisStackContainer(DEFAULT_IMAGE_NAME.withTag("edge")).withReuse(true);
    REDIS.start();
  }

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
  }

  @Autowired
  ApplicationContext applicationContext;

  @Test
  void applicationContextStartsWithRedisTemplateRefDefaults() {
    assertThat(applicationContext.containsBean("redisOmTemplate")).isTrue();
    assertThat(applicationContext.containsBean("redisTemplate")).isTrue();
  }
}
