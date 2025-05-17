package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.ColonInPrefix;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface ColonInPrefixRepository extends RedisDocumentRepository<ColonInPrefix, String> {
  Optional<ColonInPrefix> findOneByName(String name);
}
