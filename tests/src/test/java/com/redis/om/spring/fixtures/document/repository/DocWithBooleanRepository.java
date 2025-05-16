package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithBoolean;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface DocWithBooleanRepository extends RedisDocumentRepository<DocWithBoolean, String> {
}
