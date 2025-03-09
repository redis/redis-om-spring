package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithIntegerId;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused")
public interface DocWithIntegerIdRepository extends RedisDocumentRepository<DocWithIntegerId, Integer> {
}
