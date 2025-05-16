package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.WithAlias;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface WithAliasRepository extends RedisDocumentRepository<WithAlias, String> {
  Optional<WithAlias> findFirstByNumber(Integer number);
}
