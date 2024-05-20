package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Developer;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused")
public interface DeveloperRepository extends RedisDocumentRepository<Developer, String> {

}
