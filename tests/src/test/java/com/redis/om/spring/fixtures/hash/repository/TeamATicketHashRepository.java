package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.fixtures.hash.model.TicketHash;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@IndexingOptions(
    indexName = "ticket_hash_team_a_idx", filter = "@team==\"TeamA\""
)
public interface TeamATicketHashRepository extends RedisEnhancedRepository<TicketHash, String> {
}
