package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.BadDoc;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused")
public interface BadDocRepository extends RedisDocumentRepository<BadDoc, String> {
}
