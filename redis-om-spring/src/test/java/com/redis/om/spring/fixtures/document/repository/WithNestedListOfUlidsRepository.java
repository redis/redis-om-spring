package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.WithNestedListOfUlids;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface WithNestedListOfUlidsRepository extends RedisDocumentRepository<WithNestedListOfUlids, String> {
}
