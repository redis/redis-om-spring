package com.redislabs.spring;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.mapping.RedisMappingContext;

import com.redislabs.spring.client.RedisModulesClient;
import com.redislabs.spring.ops.RedisModulesOperations;
import com.redislabs.spring.ops.json.JSONOperations;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisModulesConfiguration extends CachingConfigurerSupport {
  

  @Bean(name = "redisModulesClient")
  RedisModulesClient redisModulesClient(JedisConnectionFactory jedisConnectionFactory) {
    System.out.println("YES redisModulesClient");
    return new RedisModulesClient(jedisConnectionFactory);
  }

  @Bean(name = "redisModulesOperations")
  RedisModulesOperations<?, ?> redisModulesOperations(RedisModulesClient rmc) {
    System.out.println("YES redisModulesOperations");
    return new RedisModulesOperations<>(rmc);
  }

  @Bean(name = "redisJSONOperations")
  JSONOperations<?> redisJSONOperations(RedisModulesOperations<?, ?> redisModulesOperations) {
    System.out.println("YES redisJSONOperations");
    return redisModulesOperations.opsForJSON();
  }
  
  @Bean(name = "redisTemplate")
  @Primary
  public RedisTemplate<?, ?> redisTemplate(JedisConnectionFactory connectionFactory) {
    RedisTemplate<?, ?> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    return template;
  }

  @Bean(name = "redisJSONKeyValueAdapter")
  RedisJSONKeyValueAdapter getRedisJSONKeyValueAdapter(RedisOperations<?, ?> redisOps,
      JSONOperations<?> redisJSONOperations) {
    System.out.println("YES redisJSONKeyValueAdapter");
    return new RedisJSONKeyValueAdapter(redisOps, redisJSONOperations);
  }

  @Bean(name = "redisKeyValueTemplate")
  public RedisKeyValueTemplate getRedisJSONKeyValueTemplate(RedisOperations<?, ?> redisOps,
      JSONOperations<?> redisJSONOperations) {
    System.out.println("YES redisJSONKeyValueTemplate");
    RedisMappingContext mappingContext = new RedisMappingContext();
    return new RedisKeyValueTemplate(getRedisJSONKeyValueAdapter(redisOps, redisJSONOperations), mappingContext);
  }
}
