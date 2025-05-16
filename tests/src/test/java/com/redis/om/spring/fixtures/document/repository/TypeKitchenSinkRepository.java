package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;
import java.util.UUID;

import com.redis.om.spring.fixtures.document.model.TypeKitchenSink;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface TypeKitchenSinkRepository extends RedisDocumentRepository<TypeKitchenSink, String> {
  Optional<TypeKitchenSink> findFirstByUuid(UUID uuid);
}
