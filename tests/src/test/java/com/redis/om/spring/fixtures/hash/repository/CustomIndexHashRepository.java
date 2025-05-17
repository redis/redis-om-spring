package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.CustomIndexHash;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface CustomIndexHashRepository extends RedisDocumentRepository<CustomIndexHash, String> {
}
