package com.redis.om.spring.fixtures.hash.autodiscovery;

import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface AHashRepository extends RedisEnhancedRepository<AHash, String> {
}
