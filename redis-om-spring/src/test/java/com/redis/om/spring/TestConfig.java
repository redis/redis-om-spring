package com.redis.om.spring;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import redis.clients.jedis.JedisPoolConfig;

public class TestConfig {
  @Autowired
  Environment env;

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    String host = env.getProperty("spring.redis.host");
    int port = env.getProperty("spring.redis.port", Integer.class);

    RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration(host, port);

    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setTestWhileIdle(true);
    poolConfig.setMinEvictableIdleTime(Duration.ofMillis(60000));
    poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
    poolConfig.setNumTestsPerEvictionRun(-1);

    final Integer timeout = 10000;

    final JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder()
        .connectTimeout(Duration.ofMillis(timeout)).readTimeout(Duration.ofMillis(timeout)).usePooling()
        .poolConfig(poolConfig).build();

    return new JedisConnectionFactory(conf, jedisClientConfiguration);
  }

  @Bean
  public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<?, ?> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    return template;
  }
}
