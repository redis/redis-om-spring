package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Doc;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface DocRepository extends RedisDocumentRepository<Doc, String> {
}
