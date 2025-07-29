package com.redis.om.spring.fixtures.hash.repository;

import java.util.List;

import com.redis.om.spring.fixtures.hash.model.HashWithCustomPrefixAndRedisKey;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface HashWithCustomPrefixAndRedisKeyRepository extends RedisEnhancedRepository<HashWithCustomPrefixAndRedisKey, String> {
  List<HashWithCustomPrefixAndRedisKey> findByName(String name);
}