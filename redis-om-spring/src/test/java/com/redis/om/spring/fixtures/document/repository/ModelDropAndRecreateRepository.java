package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.ModelDropAndRecreate;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface ModelDropAndRecreateRepository extends RedisDocumentRepository<ModelDropAndRecreate, String> {
}
