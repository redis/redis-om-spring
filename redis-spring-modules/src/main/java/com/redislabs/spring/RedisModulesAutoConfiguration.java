package com.redislabs.spring;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.redislabs.spring.client.RedisModulesClient;
import com.redislabs.spring.ops.RedisModulesOperations;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisModulesAutoConfiguration {

	@Bean(name = "redisModulesClient")
	RedisModulesClient redisModulesClient(JedisConnectionFactory jedisConnectionFactory) {
		return new RedisModulesClient(jedisConnectionFactory);
	}
	
	@Bean(name = "redisModulesOperations")
	RedisModulesOperations<?, ?> redisModulesOperations(RedisModulesClient rmc) {
    return new RedisModulesOperations<>(rmc);
	}



}
