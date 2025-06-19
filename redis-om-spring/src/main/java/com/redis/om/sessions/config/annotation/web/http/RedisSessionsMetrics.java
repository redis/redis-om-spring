/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.config.annotation.web.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redis.om.sessions.MetricsMonitor;
import com.redis.om.sessions.RedisSession;
import com.redis.om.sessions.RedisSessionProvider;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class RedisSessionsMetrics {
  @Bean
  public MetricsMonitor<RedisSession> metricsMonitor(RedisSessionProvider provider, MeterRegistry registry,
      RedisSessionProperties properties) {
    return new MetricsMonitor<>(registry, provider, provider.getAppPrefix(), 5, properties.getSessionSizeQuantiles());
  }
}
