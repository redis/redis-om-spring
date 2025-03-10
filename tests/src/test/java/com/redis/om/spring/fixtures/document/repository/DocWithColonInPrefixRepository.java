package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithColonInPrefix;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocWithColonInPrefixRepository extends RedisDocumentRepository<DocWithColonInPrefix, String> {
}
