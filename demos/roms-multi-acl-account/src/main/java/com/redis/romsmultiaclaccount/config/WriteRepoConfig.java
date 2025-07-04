package com.redis.romsmultiaclaccount.config;

import org.springframework.context.annotation.Configuration;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

@Configuration
@EnableRedisDocumentRepositories(
    basePackages = { "com.redis.romsmultiaclaccount.repository.write", "com.redis.romsmultiaclaccount.model" },
    keyValueTemplateRef = "writeKeyValueTemplate", redisTemplateRef = "writeRedisOperations"
)
class WriteRepoConfig {
}