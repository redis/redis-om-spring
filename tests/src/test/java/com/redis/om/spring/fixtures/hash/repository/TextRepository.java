package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.Text;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface TextRepository extends RedisEnhancedRepository<Text, String> {
}
