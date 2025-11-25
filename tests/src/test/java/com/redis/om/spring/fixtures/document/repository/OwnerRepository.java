package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Owner;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface OwnerRepository extends RedisDocumentRepository<Owner, String> {
}
