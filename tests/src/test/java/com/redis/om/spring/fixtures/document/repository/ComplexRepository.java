package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Complex;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface ComplexRepository extends RedisDocumentRepository<Complex, String> {
}
