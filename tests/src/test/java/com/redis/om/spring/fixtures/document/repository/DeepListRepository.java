package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DeepList;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DeepListRepository extends RedisDocumentRepository<DeepList, String> {
}
