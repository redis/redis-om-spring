package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.Country;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface CountryRepository extends RedisEnhancedRepository<Country, String> {
}
