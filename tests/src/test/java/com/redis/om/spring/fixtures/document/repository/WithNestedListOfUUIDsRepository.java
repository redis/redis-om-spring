package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.WithNestedListOfUUIDs;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface WithNestedListOfUUIDsRepository extends RedisDocumentRepository<WithNestedListOfUUIDs, String> {
}
