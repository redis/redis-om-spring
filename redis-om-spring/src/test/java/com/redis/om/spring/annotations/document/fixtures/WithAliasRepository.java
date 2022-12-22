package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

@SuppressWarnings("unused") public interface WithAliasRepository extends RedisDocumentRepository<WithAlias, String> {
  Optional<WithAlias> findFirstByNumber(Integer number);
}
