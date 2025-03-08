package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.HashWithVectors;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface HashWithVectorsRepository extends RedisEnhancedRepository<HashWithVectors, String> {
}
