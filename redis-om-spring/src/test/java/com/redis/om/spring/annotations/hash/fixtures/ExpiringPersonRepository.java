package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings("unused") public interface ExpiringPersonRepository extends RedisEnhancedRepository<ExpiringPerson, String> {
}
