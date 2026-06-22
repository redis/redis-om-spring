package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.repository.RedisEnhancedRepository;
import com.redis.om.spring.fixtures.hash.model.HashWithNestedMap;

public interface HashWithNestedMapRepository extends RedisEnhancedRepository<HashWithNestedMap, String> {
}
