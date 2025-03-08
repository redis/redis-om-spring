package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Doc3;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused")
public interface Doc3Repository extends RedisDocumentRepository<Doc3, String> {
}
