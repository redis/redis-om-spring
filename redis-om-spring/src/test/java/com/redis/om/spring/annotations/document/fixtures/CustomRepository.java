package com.redis.om.spring.annotations.document.fixtures;

import java.util.Optional;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface CustomRepository extends RedisDocumentRepository<Custom, Long> {
  Optional<Custom> findFirstByName(String name);
}
