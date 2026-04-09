package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.ExpiringPersonDirectFieldAccess;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface ExpiringPersonDirectFieldAccessRepository
    extends RedisDocumentRepository<ExpiringPersonDirectFieldAccess, String> {
}
