package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.State;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface StateRepository extends RedisDocumentRepository<State, String> {
}
