package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.States;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface StatesRepository extends RedisDocumentRepository<States, String> {
}
