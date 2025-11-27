package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DynamicIndexEntity;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DynamicIndexEntityRepository extends RedisDocumentRepository<DynamicIndexEntity, String> {

    Iterable<DynamicIndexEntity> findByName(String name);
}