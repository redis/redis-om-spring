package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.ASimpleHash;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface ASimpleHashRepository extends RedisEnhancedRepository<ASimpleHash, String> {
}
