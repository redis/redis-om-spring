package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.ModelSkipIfExist;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface ModelSkipIfExistsRepository extends RedisDocumentRepository<ModelSkipIfExist, String> {
}
