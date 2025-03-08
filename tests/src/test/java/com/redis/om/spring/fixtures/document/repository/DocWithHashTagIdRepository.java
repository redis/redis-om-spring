package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithHashTagId;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings("unused")
public interface DocWithHashTagIdRepository extends RedisEnhancedRepository<DocWithHashTagId, String> {
}
