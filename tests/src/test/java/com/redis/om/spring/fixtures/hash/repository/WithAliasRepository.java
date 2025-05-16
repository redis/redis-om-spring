package com.redis.om.spring.fixtures.hash.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.hash.model.WithAlias;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings(
  "unused"
)
public interface WithAliasRepository extends RedisEnhancedRepository<WithAlias, String> {
  Optional<WithAlias> findFirstByNumber(Integer number);
}
