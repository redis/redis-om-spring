package com.redis.om.spring;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.vectorize.Embedder;
import com.redis.testcontainers.RedisStackContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Comparator;

import static com.redis.testcontainers.RedisStackContainer.DEFAULT_IMAGE_NAME;

@SuppressWarnings({ "SpellCheckingInspection", "resource" })
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
public abstract class AbstractBaseOMTest {
  @Container
  static final RedisStackContainer REDIS;

  static {
    REDIS = new RedisStackContainer(DEFAULT_IMAGE_NAME.withTag(RedisStackContainer.DEFAULT_TAG)).withReuse(true);
    REDIS.start();
  }

  @Autowired
  @Qualifier("featureExtractor")
  public Embedder embedder;
  @Autowired
  protected StringRedisTemplate template;
  @Autowired
  protected RedisModulesOperations<String> modulesOperations;
  @Autowired
  @Qualifier("redisCustomKeyValueTemplate")
  protected CustomRedisKeyValueTemplate kvTemplate;
  @Autowired
  protected RediSearchIndexer indexer;
  @Autowired
  @Qualifier("omGsonBuilder")
  public GsonBuilder gsonBuilder;
  @Autowired
  public RedisOperations<?, ?> redisOperations;

  protected Comparator<Double> closeToComparator = new Comparator<Double>() {
    @Override
    public int compare(Double o1, Double o2) {
      return Math.abs(o1.doubleValue() - o2.doubleValue()) < 0.001 ? 0 : -1;
    }
  };

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
