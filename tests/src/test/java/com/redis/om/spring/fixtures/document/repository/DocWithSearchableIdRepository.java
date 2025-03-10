package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithSearchableId;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocWithSearchableIdRepository extends RedisDocumentRepository<DocWithSearchableId, String> {
}
