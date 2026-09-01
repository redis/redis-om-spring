package com.redis.om.spring.issues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.mapping.RedisMappingContext;

import com.redis.om.spring.RedisModulesConfiguration;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.mapping.RedisEnhancedMappingContext;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.pds.BloomOperations;
import com.redis.om.spring.ops.pds.CountMinSketchOperations;
import com.redis.om.spring.ops.pds.CuckooFilterOperations;

class Issue770RediSearchIndexerBeanOverrideTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withUserConfiguration(
      UserIndexerConfig.class, RedisModulesConfiguration.class).withPropertyValues("spring.main.lazy-initialization=true");

  @Test
  void rediSearchIndexerBeanCanBeOverriddenByName() {
    contextRunner.run(context -> {
      assertThat(context).hasSingleBean(RediSearchIndexer.class);
      assertThat(context).getBean("rediSearchIndexer").isSameAs(context.getBean("customIndexer"));
    });
  }

  @Configuration(
      proxyBeanMethods = false
  )
  static class UserIndexerConfig {

    @Bean
    static BeanFactoryPostProcessor lazyInitPostProcessor() {
      return beanFactory -> {
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
          beanFactory.getBeanDefinition(beanName).setLazyInit(true);
        }
      };
    }

    @Bean(
        name = { "customIndexer", "rediSearchIndexer" }
    )
    RediSearchIndexer customIndexer() {
      return mock(RediSearchIndexer.class);
    }

    @Bean
    RedisModulesOperations<?> redisModulesOperations() {
      RedisModulesOperations<?> redisModulesOperations = mock(RedisModulesOperations.class);
      when(redisModulesOperations.opsForJSON()).thenReturn(mock(JSONOperations.class));
      when(redisModulesOperations.opsForBloom()).thenReturn(mock(BloomOperations.class));
      when(redisModulesOperations.opsForCuckoFilter()).thenReturn(mock(CuckooFilterOperations.class));
      when(redisModulesOperations.opsForCountMinSketch()).thenReturn(mock(CountMinSketchOperations.class));
      return redisModulesOperations;
    }

    @Bean(
        name = "redisEnhancedMappingContext"
    )
    RedisMappingContext redisMappingContext() {
      return new RedisEnhancedMappingContext();
    }
  }
}
