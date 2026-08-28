package com.redis.om.spring.issues;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.mapping.RedisMappingContext;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.RedisModulesConfiguration;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.ops.RedisModulesOperations;

class Issue770RediSearchIndexerBeanOverrideTest {

  @Test
  void rediSearchIndexerBeanCanBeOverriddenByName() throws NoSuchMethodException {
    Method factoryMethod = RedisModulesConfiguration.class.getDeclaredMethod("redisearchIndexer",
        ApplicationContext.class, RedisOMProperties.class, GsonBuilder.class, RedisModulesOperations.class,
        RedisMappingContext.class);

    ConditionalOnMissingBean condition = factoryMethod.getAnnotation(ConditionalOnMissingBean.class);

    assertThat(condition).isNotNull();
    assertThat(condition.name()).containsExactly("rediSearchIndexer");
  }
}
