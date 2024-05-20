package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.HashWithByteArrayFlatVector;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface HashWithByteArrayFlatVectorRepository
  extends RedisEnhancedRepository<HashWithByteArrayFlatVector, String> {
}
