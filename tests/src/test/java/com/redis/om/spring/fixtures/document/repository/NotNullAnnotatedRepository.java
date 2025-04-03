package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.NotNullAnnotated;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface NotNullAnnotatedRepository extends RedisDocumentRepository<NotNullAnnotated, String> {
}
