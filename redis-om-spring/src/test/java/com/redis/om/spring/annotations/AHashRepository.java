package com.redis.om.spring.annotations;

import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings("unused") public interface AHashRepository extends RedisEnhancedRepository<AHash, String> {
}
