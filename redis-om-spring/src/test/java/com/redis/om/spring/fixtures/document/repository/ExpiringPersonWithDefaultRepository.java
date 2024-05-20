package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.ExpiringPersonWithDefault;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused")
public interface ExpiringPersonWithDefaultRepository
  extends RedisDocumentRepository<ExpiringPersonWithDefault, String> {
}
