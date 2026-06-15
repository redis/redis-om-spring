package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Ticket;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface AllTicketsRepository extends RedisDocumentRepository<Ticket, String> {
}
