/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.config.annotation.web.http;

import java.time.Duration;
import java.util.Optional;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.session.FlushMode;
import org.springframework.session.MapSession;
import org.springframework.session.SaveMode;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;

import com.redis.lettucemod.RedisModulesClient;
import com.redis.om.sessions.RedisSessionProvider;
import com.redis.om.sessions.RedisSessionProviderConfiguration;
import com.redis.om.sessions.RedisSessionRepository;
import com.redis.om.sessions.indexing.RedisIndexConfiguration;

@Configuration
@Import(
  SpringHttpSessionConfiguration.class
)
@EnableConfigurationProperties(
  RedisSessionProperties.class
)
public class RedisSessionsConfiguration {
  private Duration maxInactiveInterval = MapSession.DEFAULT_MAX_INACTIVE_INTERVAL;
  private String redisAppPrefix = RedisSessionRepository.DEFAULT_KEY_NAMESPACE;
  private FlushMode flushMode = FlushMode.ON_SAVE;
  private SaveMode saveMode = SaveMode.ON_SET_ATTRIBUTE;

  @Bean
  public RedisModulesClient redisClient(RedisSessionProperties properties) {
    String redisUri = String.format("redis://%s:%d", properties.getHost(), properties.getPort());
    return RedisModulesClient.create(redisUri);
  }

  @Bean
  public RedisSessionProvider redisSessionProvider(RedisModulesClient client,
      Optional<RedisIndexConfiguration> redisIndexConfigurationOpt, RedisSessionProperties properties) {

    RedisIndexConfiguration redisIndexConfiguration = redisIndexConfigurationOpt.orElse(RedisIndexConfiguration
        .builder().build());
    RedisSessionProviderConfiguration config = RedisSessionProviderConfiguration.builder().appPrefix(properties
        .getPrefix()).localCacheMaxSize(properties.getCache().getCap()).indexConfiguration(redisIndexConfiguration)
        .minLocalRecordSize(properties.getCache().getMin()).build();
    return RedisSessionProvider.create(client, config);
  }

  public void createIndex(RedisSessionProvider provider) {
    provider.bootstrap();
  }

  @Bean
  public RedisSessionRepository redisSessionRepository(RedisSessionProvider provider) {
    return new RedisSessionRepository(provider);
  }

  @Bean(
      initMethod = "createIndex"
  )
  public StartupBean startup() {
    return new StartupBean();
  }

}
