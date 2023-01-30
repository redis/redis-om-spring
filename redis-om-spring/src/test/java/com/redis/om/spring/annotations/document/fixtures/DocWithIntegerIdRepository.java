package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused") public interface DocWithIntegerIdRepository extends RedisDocumentRepository<DocWithIntegerId, Integer> {
}
