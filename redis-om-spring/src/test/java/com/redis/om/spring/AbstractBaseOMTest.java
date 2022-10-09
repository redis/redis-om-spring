package com.redis.om.spring;

import static com.redis.testcontainers.RedisModulesContainer.DEFAULT_IMAGE_NAME;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.testcontainers.RedisModulesContainer;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
public abstract class AbstractBaseOMTest {
  @Container
  static final RedisModulesContainer REDIS;

  static {
    REDIS = new RedisModulesContainer(DEFAULT_IMAGE_NAME.withTag("edge")).withReuse(true);
    REDIS.start();
  }

  @Autowired
  protected RedisTemplate<String, String> template;

  @Autowired
  protected RedisModulesOperations<String> modulesOperations;

  @Autowired
  @Qualifier("redisCustomKeyValueTemplate")
  protected CustomRedisKeyValueTemplate kvTemplate;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.redis.host", REDIS::getHost);
    registry.add("spring.redis.port", REDIS::getFirstMappedPort);
  }

  protected void flushSearchIndexFor(Class<?> entityClass) {
    SearchOperations<String> searchOps = modulesOperations.opsForSearch(entityClass.getName() + "Idx");
    Set<String> docKeys = template.keys(String.format("%s:*", entityClass.getName()));
    searchOps.deleteDocuments(false, docKeys.toArray(String[]::new));
  }

}
