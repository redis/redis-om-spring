package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.CustomIndexDoc;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused")
public interface CustomIndexDocRepository extends RedisDocumentRepository<CustomIndexDoc, String> {
}
