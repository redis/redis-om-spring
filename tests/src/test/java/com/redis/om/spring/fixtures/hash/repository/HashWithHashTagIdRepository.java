package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.HashWithHashTagId;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings(
  "unused"
)
public interface HashWithHashTagIdRepository extends RedisEnhancedRepository<HashWithHashTagId, String> {
}
