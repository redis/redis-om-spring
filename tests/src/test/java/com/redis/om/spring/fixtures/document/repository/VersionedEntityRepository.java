package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.VersionedEntity;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface VersionedEntityRepository extends RedisDocumentRepository<VersionedEntity, Long> {
}
