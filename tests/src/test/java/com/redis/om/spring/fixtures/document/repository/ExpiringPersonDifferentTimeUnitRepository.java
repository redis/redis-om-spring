package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.ExpiringPersonDifferentTimeUnit;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface ExpiringPersonDifferentTimeUnitRepository extends
    RedisDocumentRepository<ExpiringPersonDifferentTimeUnit, String> {
}
