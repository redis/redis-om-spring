package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.ExpiringPersonDifferentTimeUnit;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings("unused")
public interface ExpiringPersonDifferentTimeUnitRepository
  extends RedisEnhancedRepository<ExpiringPersonDifferentTimeUnit, String> {
}
