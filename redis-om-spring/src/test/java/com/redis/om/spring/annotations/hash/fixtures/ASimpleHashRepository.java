package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface ASimpleHashRepository extends RedisEnhancedRepository<ASimpleHash, String> {
}
