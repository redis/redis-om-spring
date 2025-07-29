package com.redis.om.spring.fixtures.hash.repository;

import java.util.List;

import com.redis.om.spring.fixtures.hash.model.HashWithRedisKey;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface HashWithRedisKeyRepository extends RedisEnhancedRepository<HashWithRedisKey, String> {
  List<HashWithRedisKey> findByName(String name);
}