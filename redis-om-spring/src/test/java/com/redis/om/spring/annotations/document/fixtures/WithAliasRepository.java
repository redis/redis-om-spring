package com.redis.om.spring.annotations.document.fixtures;

import java.util.Optional;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface WithAliasRepository extends RedisDocumentRepository<WithAlias, String> {
  Optional<WithAlias> findFirstByNumber(Integer number);
}
