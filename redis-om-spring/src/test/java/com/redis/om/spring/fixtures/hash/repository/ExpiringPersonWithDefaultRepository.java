package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.ExpiringPersonWithDefault;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings("unused")
public interface ExpiringPersonWithDefaultRepository
  extends RedisEnhancedRepository<ExpiringPersonWithDefault, String> {
}
