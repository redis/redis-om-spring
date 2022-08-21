package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface ExpiringPersonDifferentTimeUnitRepository
    extends RedisEnhancedRepository<ExpiringPersonDifferentTimeUnit, String> {
}
