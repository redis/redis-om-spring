package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.TooManyReferences;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface TooManyReferencesRepository extends RedisDocumentRepository<TooManyReferences, String> {
}
