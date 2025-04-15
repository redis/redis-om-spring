package com.redis.om.amr.entraid.config;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableRedisDocumentRepositories(basePackages = "com.redis.om.amr.entraid.*")
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, String> redisTemplate(JedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

}