package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.fixtures.hash.model.TicketHash;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@IndexingOptions(
    indexName = "ticket_hash_team_b_idx", filter = "@team==\"TeamB\""
)
public interface TeamBTicketHashRepository extends RedisEnhancedRepository<TicketHash, String> {
}
