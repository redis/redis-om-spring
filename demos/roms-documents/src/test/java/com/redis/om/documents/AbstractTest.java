package com.redis.om.documents;

import static com.redis.testcontainers.RedisStackContainer.DEFAULT_IMAGE_NAME;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.redis.om.spring.CustomRedisKeyValueTemplate;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.testcontainers.RedisStackContainer;

@Testcontainers(
    disabledWithoutDocker = true
)
@DirtiesContext
public abstract class AbstractTest {
  @Container
  static final RedisStackContainer REDIS;

  static {
    REDIS = new RedisStackContainer(DEFAULT_IMAGE_NAME.withTag("edge")).withReuse(true);
    REDIS.start();
  }

  @Autowired
  protected StringRedisTemplate template;

  @Autowired
  protected RedisModulesOperations<String> modulesOperations;

  @Autowired
  @Qualifier(
    "redisCustomKeyValueTemplate"
  )
  protected CustomRedisKeyValueTemplate kvTemplate;

  @Autowired
  protected RediSearchIndexer indexer;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.redis.host", REDIS::getHost);
    registry.add("spring.redis.port", REDIS::getFirstMappedPort);
  }

  protected void flushSearchIndexFor(Class<?> entityClass) {
    indexer.dropIndexAndDocumentsFor(entityClass);
    indexer.createIndexFor(entityClass);
  }

}
