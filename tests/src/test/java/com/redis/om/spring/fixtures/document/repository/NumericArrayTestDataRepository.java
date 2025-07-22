package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.NumericArrayTestData;
import com.redis.om.spring.repository.RedisDocumentRepository;

/**
 * Repository for NumericArrayTestData to demonstrate GitHub issue #400
 */
public interface NumericArrayTestDataRepository extends RedisDocumentRepository<NumericArrayTestData, String> {
}