package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.ExpiringPerson;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings(
  "unused"
)
public interface ExpiringPersonRepository extends RedisEnhancedRepository<ExpiringPerson, String> {
}
