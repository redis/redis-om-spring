package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface ScheduledRepository extends RedisDocumentRepository<Scheduled, String> {
}