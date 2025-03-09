package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.NonIndexedHash;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings("unused")
public interface NonIndexedHashRepository extends RedisEnhancedRepository<NonIndexedHash, String> {
}
