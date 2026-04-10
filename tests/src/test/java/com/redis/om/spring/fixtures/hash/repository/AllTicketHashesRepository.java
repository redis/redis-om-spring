package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.TicketHash;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface AllTicketHashesRepository extends RedisEnhancedRepository<TicketHash, String> {
}
