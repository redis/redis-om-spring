package com.redis.om.streams.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.redis.om.streams.Producer;
import com.redis.om.streams.annotation.EnableRedisStreams;
import com.redis.om.streams.command.serial.TopicProducer;

import jakarta.annotation.PostConstruct;
import redis.clients.jedis.JedisPooled;

@Configuration
@EnableRedisStreams(
    basePackages = "com.redis.om.streams.consumer"
)
public class RedisStreamsConfiguration {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Value(
    "${spring.data.redis.host}"
  )
  private String host;
  @Value(
    "${spring.data.redis.port}"
  )
  private int port;
  @Value(
    "${spring.data.redis.username}"
  )
  private String username;
  @Value(
    "${spring.data.redis.password}"
  )
  private String password;

  @PostConstruct
  private void init() {
    logger.info("{} init", getClass().getSimpleName());
  }

  @Bean
  public JedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(host);
    redisStandaloneConfiguration.setPort(port);
    redisStandaloneConfiguration.setUsername(username);
    redisStandaloneConfiguration.setPassword(password);
    JedisConnectionFactory jediConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
    jediConnectionFactory.setConvertPipelineAndTxResults(false);
    return jediConnectionFactory;
  }

  @Bean
  public JedisPooled jedisPooled() {
    logger.info("Creating JedisPooled");
    return new JedisPooled(host, port);
  }

  @Bean
  public Producer topicProducer(JedisPooled jedisPooled) {
    logger.info("Creating TopicProducer");
    return new TopicProducer(jedisPooled, "topicFoo");
  }

}