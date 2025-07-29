package com.redis.om.spring.fixtures.document.repository;

import java.util.List;

import com.redis.om.spring.fixtures.document.model.DocWithRedisKey;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocWithRedisKeyRepository extends RedisDocumentRepository<DocWithRedisKey, String> {
  List<DocWithRedisKey> findByName(String name);
}