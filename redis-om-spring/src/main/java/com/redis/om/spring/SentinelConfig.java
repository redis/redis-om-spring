package com.redis.om.spring;

import static org.springframework.util.StringUtils.commaDelimitedListToSet;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import redis.clients.jedis.JedisPoolConfig;

public class SentinelConfig {
  @Bean
  public JedisConnectionFactory jedisConnectionFactory(Environment env) {
    String master = env.getProperty("spring.redis.sentinel.master", "localhost");
    String nodes = env.getProperty("spring.redis.sentinel.nodes");
    Set<String> sentinelNodes = commaDelimitedListToSet(nodes);
    Set<RedisNode> redisNodes = sentinelNodes.stream().map(RedisNode::fromString).collect(Collectors.toSet());

    RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration().master(master);
    sentinelConfig.setSentinels(redisNodes);

    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
    poolConfig.setNumTestsPerEvictionRun(-1);
    poolConfig.setTestWhileIdle(false);
    poolConfig.setTestOnReturn(false);
    poolConfig.setTestOnBorrow(false);

    final int timeout = 10000;

    final JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().connectTimeout(Duration
        .ofMillis(timeout)).readTimeout(Duration.ofMillis(timeout)).usePooling().poolConfig(poolConfig).build();

    return new JedisConnectionFactory(sentinelConfig, jedisClientConfiguration);
  }

  @Bean
  public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
    StringRedisTemplate template = new StringRedisTemplate();
    template.setConnectionFactory(connectionFactory);

    return template;
  }
}