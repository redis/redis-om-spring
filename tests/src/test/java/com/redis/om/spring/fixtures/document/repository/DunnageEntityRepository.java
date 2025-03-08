package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DunnageEntity;
import com.redis.om.spring.fixtures.document.model.DunnageId;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DunnageEntityRepository extends RedisDocumentRepository<DunnageEntity, DunnageId> {
}
