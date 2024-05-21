package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.ModelSkipAlways;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface ModelSkipAlwaysRepository extends RedisDocumentRepository<ModelSkipAlways, String> {
}
