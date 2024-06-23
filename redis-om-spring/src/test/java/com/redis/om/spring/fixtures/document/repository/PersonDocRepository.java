package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.PersonDoc;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface PersonDocRepository extends RedisDocumentRepository<PersonDoc, String> {
}
