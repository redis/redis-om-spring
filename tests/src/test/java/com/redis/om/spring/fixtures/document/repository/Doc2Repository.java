package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Doc2;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface Doc2Repository extends RedisDocumentRepository<Doc2, String> {
}
