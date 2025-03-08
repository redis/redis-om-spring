package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.HashWithColonInPrefix;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface HashWithColonInPrefixRepository extends RedisEnhancedRepository<HashWithColonInPrefix, String> {
}
