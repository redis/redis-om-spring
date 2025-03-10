package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.CompanyWithLongId;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused")
public interface CompanyWithLongIdRepository extends RedisDocumentRepository<CompanyWithLongId, Long> {
}
