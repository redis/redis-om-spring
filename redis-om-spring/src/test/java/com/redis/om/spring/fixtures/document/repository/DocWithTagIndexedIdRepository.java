package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithTagIndexedId;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocWithTagIndexedIdRepository extends RedisDocumentRepository<DocWithTagIndexedId, String> {
}
