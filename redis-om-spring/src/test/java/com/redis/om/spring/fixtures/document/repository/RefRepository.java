package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Ref;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface RefRepository extends RedisDocumentRepository<Ref, String> {
}
