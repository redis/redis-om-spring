package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithCustomNameId;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface DocWithCustomNameIdRepository extends RedisDocumentRepository<DocWithCustomNameId, String> {
}
