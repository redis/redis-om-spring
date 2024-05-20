package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithLong;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused")
public interface DocWithLongRepository extends RedisDocumentRepository<DocWithLong, String> {
}
