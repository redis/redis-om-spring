package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface CountryRepository extends RedisEnhancedRepository<Country, String> {
}
