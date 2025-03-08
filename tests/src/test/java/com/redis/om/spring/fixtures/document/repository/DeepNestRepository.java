package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DeepNest;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

@SuppressWarnings("unused")
public interface DeepNestRepository extends RedisDocumentRepository<DeepNest, String> {
  Optional<DeepNest> findFirstByNameIs(String name);
}
