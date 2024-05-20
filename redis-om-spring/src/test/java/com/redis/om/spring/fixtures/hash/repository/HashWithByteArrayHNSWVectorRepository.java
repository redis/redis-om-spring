package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.HashWithByteArrayHNSWVector;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface HashWithByteArrayHNSWVectorRepository
  extends RedisEnhancedRepository<HashWithByteArrayHNSWVector, String> {
}
