package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DeepNestNonIndexed;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface DeepNestNonIndexedRepository extends RedisDocumentRepository<DeepNestNonIndexed, String> {
}
