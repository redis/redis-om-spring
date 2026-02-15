package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.HashWithTextAndVector;
import com.redis.om.spring.repository.RedisEnhancedRepository;

/**
 * Repository for HashWithTextAndVector test fixture.
 * Used for hybrid search integration tests.
 */
public interface HashWithTextAndVectorRepository extends RedisEnhancedRepository<HashWithTextAndVector, String> {
}
