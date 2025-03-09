package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.SomeDocument;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused")
public interface SomeDocumentRepository extends RedisDocumentRepository<SomeDocument, String> {
}
