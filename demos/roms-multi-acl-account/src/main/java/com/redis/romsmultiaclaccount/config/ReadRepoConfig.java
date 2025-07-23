package com.redis.romsmultiaclaccount.config;

import org.springframework.context.annotation.Configuration;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

@Configuration
@EnableRedisDocumentRepositories(
    basePackages = { "com.redis.romsmultiaclaccount.repository.read", "com.redis.romsmultiaclaccount.model" },
    keyValueTemplateRef = "readKeyValueTemplate", redisTemplateRef = "readRedisOperations"
)
class ReadRepoConfig {
}