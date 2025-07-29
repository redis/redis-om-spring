package com.redis.om.spring.fixtures.document.repository;

import java.util.List;

import com.redis.om.spring.fixtures.document.model.DocWithCustomPrefixAndRedisKey;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocWithCustomPrefixAndRedisKeyRepository extends RedisDocumentRepository<DocWithCustomPrefixAndRedisKey, String> {
  List<DocWithCustomPrefixAndRedisKey> findByName(String name);
}