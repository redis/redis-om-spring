package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.fixtures.document.model.Ticket;
import com.redis.om.spring.repository.RedisDocumentRepository;

@IndexingOptions(indexName = "ticket_team_a_idx", filter = "@team==\"TeamA\"")
public interface TeamATicketRepository extends RedisDocumentRepository<Ticket, String> {
}
