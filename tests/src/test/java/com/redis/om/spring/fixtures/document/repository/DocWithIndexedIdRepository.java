package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithIndexedId;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocWithIndexedIdRepository extends RedisDocumentRepository<DocWithIndexedId, String> {
}
