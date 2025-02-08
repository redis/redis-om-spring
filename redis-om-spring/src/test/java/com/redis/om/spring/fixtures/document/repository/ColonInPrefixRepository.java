package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.ColonInPrefix;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

public interface ColonInPrefixRepository extends RedisDocumentRepository<ColonInPrefix, String> {
  Optional<ColonInPrefix> findOneByName(String name);
}
